# Deploying traits

traits deploys to an EPFL box as a single container (SQLite is an in-process
file, so there's no separate DB service). traits publishes on
**`127.0.0.1:8090`**, carried over from the previous box where another service
held 8080. Nothing holds 8080 here, but the host's reverse proxy is configured
against 8090, so moving it means editing both. That proxy terminates TLS and
forwards the subdomain to it.

Everything lives in [`backend/deploy/`](../backend/deploy/): `Dockerfile`,
`entrypoint.sh`, `docker-compose.yml`, `.env.example`, and `deploy.sh`.

## Target

- **Host**: `traits@icvm0191.epfl.ch`
- **Compose dir**: `/home/traits/compose/traits`
- **Internal**: container listens on `8080`, published to `127.0.0.1:8090`
- **Public URL**: `https://traits.scala-lang.org` (CNAME → `icvm0191.epfl.ch`)

Host and remote dir are hard-coded near the top of
[`backend/deploy/deploy.sh`](../backend/deploy/deploy.sh) — edit there if the box moves.

Containers run on **rootless Podman**, not Docker. The `docker` CLI and its
`compose` plugin are pointed at `unix://$XDG_RUNTIME_DIR/podman/podman.sock`
by `DOCKER_HOST`, exported from `~/.profile`. Only a *login* shell reads that
file, so every non-interactive remote command — in `deploy.sh` and in this doc
alike — goes through `bash -lc`. Without it `DOCKER_HOST` is unset, the CLI
falls back to `/var/run/docker.sock`, and you get `permission denied`.

Rootless Podman is also why nothing needs `sudo` — just as well, since the
`traits` user has no passwordless sudo and is in no `docker` group.
`deploy.sh` drives Compose **v2** (`docker compose`). The box also still has
the unmaintained v1 `docker-compose` 1.29.2, which created the original
container; v2 adopts it and the `traits_traits-data` volume unchanged.

```
public internet
   │  HTTPS  (traits.scala-lang.org, Let's Encrypt cert on the host)
   ▼
host reverse proxy ──► http://127.0.0.1:8090 ──► traits-backend container
                                                  (tapir-netty-sync; /api, /docs, SPA)
                                                      └── traits-data volume (SQLite)
```

The SQLite store lives on the `traits-data` named volume. On the **first** boot
the container seeds it from the dataset baked into the image (the curated DB
snapshotted at deploy time); afterwards the volume persists, so edits made on
the live site survive redeploys.

## One-time setup

### 1. DNS

`traits.scala-lang.org` must resolve to `icvm0191.epfl.ch` (A record, or a
CNAME to the box). Make sure `:80`/`:443` are reachable — needed for the Let's
Encrypt HTTP-01 challenge, both at issuance and on every renewal.

### 2. Push infra + build, from your laptop

```sh
./backend/deploy/deploy.sh --infra
```

This builds the frontend + fat jar, snapshots the local DB as the seed, uploads
everything, and runs `docker compose up -d --build`. **The first run will fail
at container start because `.env` doesn't exist yet — that's expected**, fix it
next.

### 3. Write `.env` on the server

```sh
ssh -J traits@alaska.epfl.ch traits@icvm0191.epfl.ch
cd /home/traits/compose/traits
cp /dev/stdin .env <<'EOF'
# paste backend/deploy/.env.example and fill in the blanks:
TRAITS_ENV=prod
TRAITS_SESSION_SECRET=<openssl rand -hex 32>
TRAITS_EDITOR_PASSWORD=<a real password — NOT let-me-in>
EOF
chmod 600 .env

docker compose up -d
docker compose logs -f traits-backend     # watch startup; expect "seeding …" then the server line
```

`.env` is **never** overwritten by `deploy.sh`. Reads are public; the editor
password is all that gates create/edit/delete — share it only with reviewers.

### 4. Put a reverse proxy in front

The container speaks plain HTTP on `127.0.0.1:8090` and is not exposed
directly. TLS terminates in a reverse proxy on the host, with a Let's Encrypt
certificate. Which proxy is a host decision and deliberately not recorded here;
whatever it is, it needs to:

- serve `traits.scala-lang.org` over HTTPS and redirect `:80` → `:443`
- forward everything to `http://127.0.0.1:8090`, passing the original host and
  scheme through so generated links and the `Secure` session cookie stay correct
- rate-limit `POST /api/auth/login` — the editor password is the only thing
  between the public and write access, so this is the one endpoint worth
  throttling

Then check it end to end:

```sh
curl -s https://traits.scala-lang.org/api/health   # → {"status":"ok","entryCount":…}
```

Open `https://traits.scala-lang.org` — the boards, entry pages, version
registry and the `/docs` API browser are all served by the one container.

## Day-to-day deploys

```sh
./backend/deploy/deploy.sh          # rebuild jar + frontend + seed, restart container
./backend/deploy/deploy.sh --infra  # also re-push Dockerfile / compose / entrypoint
```

Each run installs frontend deps with `npm ci`, so a fresh checkout deploys
without any manual setup.

Each run also re-snapshots your **local** DB as the seed, but the seed only
initialises an *empty* volume — once the live site has data, redeploys keep it.
On a fresh checkout there is no local DB (`traits-data/` is gitignored); the
script then keeps the seed already on the server instead of failing.

## Pulling the live dataset down

To work locally against production data, copy the SQLite file out of the
`traits-data` volume — **`traits.sqlite` plus its `-wal`**:

```sh
mkdir -p traits-data
ssh -J traits@alaska.epfl.ch traits@icvm0191.epfl.ch \
  "bash -lc 'docker exec traits-backend tar -C /app/data -cf - traits.sqlite traits.sqlite-wal'" \
  | tar -C traits-data -xf -

# fold the WAL into the main file
python3 -c "import sqlite3; c=sqlite3.connect('traits-data/traits.sqlite'); c.execute('PRAGMA wal_checkpoint(TRUNCATE)'); c.close()"
```

The `-wal` is not optional. The DB runs in WAL mode and the live
`traits.sqlite` may not have been checkpointed for months, so copying it alone
can hand you a long-stale dataset. Skip `-shm`; SQLite rebuilds it.

The copy is only non-atomic if a write lands mid-`tar`. The JRE image has
neither `sqlite3` nor `python3`, so a proper `.backup()` snapshot can't be taken
from inside the container; for a guaranteed-consistent copy stop it first — a
clean shutdown checkpoints the WAL:

```sh
ssh -J traits@alaska.epfl.ch traits@icvm0191.epfl.ch 'bash -lc "cd /home/traits/compose/traits && docker compose stop traits-backend"'
# ... tar copy as above ...
ssh -J traits@alaska.epfl.ch traits@icvm0191.epfl.ch 'bash -lc "cd /home/traits/compose/traits && docker compose start traits-backend"'
```

Note that once a local DB exists, the next deploy snapshots it as the seed
again. That is harmless while the live volume has data — but see the next
section before dropping the volume.

## Pushing a fresh dataset (wiping live data)

If you want the live DB replaced with your current local one, drop the volume so
the next deploy re-seeds:

```sh
ssh -J traits@alaska.epfl.ch traits@icvm0191.epfl.ch 'bash -lc "cd /home/traits/compose/traits && docker compose down -v"'
./backend/deploy/deploy.sh
```

## Logs & verifying

```sh
ssh -J traits@alaska.epfl.ch traits@icvm0191.epfl.ch 'bash -lc "cd /home/traits/compose/traits && docker compose logs -f"'
curl https://traits.scala-lang.org/api/health     # entry count doubles as a readiness probe
```

json-file logging is capped at 10 MB × 5 files.
