# traits — agent instructions

`traits` tracks notable changes to Scala through their lifecycle — from an idea,
through the [SIP process](https://docs.scala-lang.org/sips/process-specification.html),
to experimental → preview → stable → removed. Public read, committee-edited.

The stack and conventions are deliberate: Scala 3 + tapir (direct style, ox) on
the backend, Scala.js + Laminar on the frontend, a shared module for the wire
contract. When in doubt about a pattern, follow the existing code. The one big
difference: storage is a single **SQLite** file holding each entry as a **JSON
document**, not a normalized Postgres schema.

> **This file describes the code; [`PLAN.md`](PLAN.md) specifies the design it
> implements** — what earns an entry, the lifecycle, the model semantics. Read
> the plan before changing the domain; read this before editing anything.

## Build & verify

Modules and how to run the app locally are in the
[README](README.md#modules); `shared` is a pure cross-project, so the HTTP
shape can't drift between the two sides.

Use `sbt` via the Bash tool.

* `sbt compile` — main compile across modules
* `sbt Test/compile` / `sbt test` — munit suites under `*/src/test/scala`
* `sbt backend/run` — start the API on `:8080` (sets `TRAITS_ENV=dev`)
* `sbt frontend/fastLinkJS` — link Scala.js for vite

If you keep an interactive session running (`sbt "~frontend/fastLinkJS"`),
`sbt -client "<task>"` reuses its incremental state and is much faster.

**`-Werror` is on**, with `-Wunused:all`, `-Wvalue-discard`, `-deprecation`,
`-feature`, `-unchecked` (see `build.sbt`). Treat every warning as a failure:

* no unused imports / locals / private members — delete them
* discard non-Unit results explicitly with `val _ = expr`
* `compile` must be green before committing

scalafmt is configured (`.scalafmt.conf`, `align.preset = more`): write
fields/case-arms with a single space and let the formatter pad the alignment —
don't align by hand. `sbt scalafmtAll` to format.

JDK 17+ is fine (no `-release` pin).

## Architecture

Request flow per concern uses a **Repo / Service / Api** triad:

* **`Endpoints`** (shared) — one `val` per route, body-only, error type `ApiError`.
  The single source of truth for the contract; the frontend derives its client
  from these same values.
* **`*Repo`** (backend) — raw Magnum `sql"…"` only. Returns the stored JSON
  document as a `String`; no upickle here.
* **`*Service`** (backend) — owns a `DataSource`, runs repo calls inside
  `connect`/`transact`, parses/serializes the document with upickle, maps to
  shared types.
* **`*Api`** (backend) — attaches server logic via `.handle` / `.handleSuccess`,
  attaches the session cookie for editor-gated routes, exposes `all:
  List[ServerEndpoint[Any, Identity]]`. Wired together in `Main`.

Server is `tapir-netty-server-sync` + `ox` — **direct style, no `Future`**. This
suits Magnum's blocking JDBC.

### Adding an endpoint

1. Add the `val` to `shared/.../Endpoints.scala` (body-only).
2. Wire server logic in the relevant `*Api.scala`. For an editor-gated write,
   add `.in(cookie[Option[String]](Endpoints.SessionCookieName))` and run
   `auth.requireEditor(cookie)` first (see `EntryApi.put`).
3. Add it to that class's `all` list (already aggregated in `Main`).
4. Add a typed wrapper to `frontend/.../Api.scala`.

## Storage (SQLite JSON document)

Two tables (`backend/.../Db.scala`):

```
entry:   slug TEXT PK | updated_at TEXT | search_text TEXT | data TEXT(JSON)
version: major INT + minor INT PK | data TEXT(JSON)
```

* `data` is the whole `Entry` / `Version` serialized with upickle. The **same
  upickle codec serves the wire and the store** — no row-class translation layer.
  `version` keys on real `major`/`minor` columns so the natural order is SQL.
* `search_text` is a denormalised lowercase blob (title + tagline + tags +
  section bodies) recomputed on every write, for `LIKE` search.
* Reads return `data` as a `String`; `EntryService.parse` does `read[Entry](_)`.
  Writes do `write(entry)` and upsert.

Magnum notes:

* Raw `sql"…"` interpolation only — **no `Repo[T]` tables**, so no SQLite
  `DbType`/dialect is needed.
* Use the **block form**: `connect(ds) { … }` for reads, `transact(ds) { … }`
  for writes. The contextual `DbCon` is in scope inside the block.
* SQLite stores JSON as plain `TEXT` — no `::jsonb` cast (that's Postgres).
* `Db.migrate` is idempotent (`CREATE TABLE IF NOT EXISTS`) and sets WAL mode.
  `PRAGMA journal_mode=WAL` must run **outside** a transaction → it's in a
  `connect` block, not `transact`. The schema is stable; what evolves is the
  document. Adding or dropping an `Entry` field needs no migration (upickle
  ignores unknown keys, defaults fill absent ones) — but **renaming an enum case
  breaks every stored row**, since the case name is itself the wire format. That
  case needs a versioned `ujson`-level step plus a `search_text` rebuild; see
  [`PLAN.md`](PLAN.md) (Architecture → evolving the stored format).
* The store is tiny and low-write; everything else (board placement, status in
  a version) is derived in code from the parsed documents, not queried in SQL.

## Domain model (`shared/.../Domain.scala`)

An **`Entry`** is one notable change to Scala; the semantics are specified in
[`PLAN.md`](PLAN.md). Key points:

* Two independent tracks. `sip: Option[Sip]` holds only the **current**
  `state: SipState` — the closed set of legal `(stage, status, recommendation)`
  combinations from the process spec, mirroring the GitHub labels on
  `scala/improvement-proposals`. `availability: List[Availability]` is the whole
  track: one `(stage, version, backport, note)` row per stage reached, where
  main-line rows carry forward and backports apply to their version only.
* `VersionId(major, minor)` is a Scala minor version, serialized as `"3.8"`
  everywhere (JSON, paths, store). The registry entity `Version` adds
  lts/released/releaseDate and lives in its own table.
* **Everything derivable is computed, never stored**: `Availability.statusIn`
  (the entry in effect for a version), `Board.cell` (board placement, including
  hidden-after-removal and the unreleased-upcoming carve-outs), and
  `Availability.validate` (the rules `EntryApi.put` enforces with a `400`). If
  you change lifecycle rules, change `Domain.scala` — the tests in
  `shared/src/test` pin the semantics, including PLAN.md's worked example.
* `sections` (freeform markdown), typed `links` with the `watch` flag, and
  `timeline` (dated non-transition events) round out the document; `archived`
  hides an entry from the boards. `EntrySummary` is the list projection — it
  keeps `sip` and `availability` so boards are computable client-side.

upickle: every domain type uses `derives ReadWriter`, including enums (parameter-
ized cases like `SipState.DesignVoteRequested(Recommendation)` are supported);
`VersionId` has a custom string codec.

tapir schemas: field-less enums need explicit string-based schemas, the
parameterized `SipState` needs `Schema.derived`, and `VersionId` has a string
schema plus a `PlainCodec` for `path[VersionId]` — all in `shared/.../Schemas.scala`.
`Endpoints` does `import Schemas.given` + `import sttp.tapir.generic.auto.*` so
case-class schemas derive on top of them. When you add an enum used in a body
type, add its `Schema` given to `Schemas`.

## Auth

`TRAITS_EDITOR_PASSWORD` is exchanged at `POST /api/auth/login` for an
HMAC-signed cookie (`SessionCodec`, `traits_session`); `AuthApi.requireEditor`
is the gate on every write. The session carries an `editor` identity string, so
per-user auth later stays localised to `AuthApi` + `login`.

## Curation by external agents

There is no in-process LLM. Curation happens over the HTTP API, against the
live OpenAPI spec at `/docs` — served by `tapir-swagger-ui-bundle` from the
actual endpoints, so it can't drift. Auth, wire shapes, the `SipState`
encoding and worked examples are in [`docs/curation.md`](docs/curation.md).

**Asked to add or update an entry? Work through
[`docs/research.md`](docs/research.md) first.** It lists the six sources an
entry is built from — SIP PR, published SIP, forum threads, release notes,
meeting notes, implementation PRs — and which field each one fills. Fetch what
you can and ask the user for the rest; the meeting notes are not public. Say
which sources don't exist for the change rather than skipping them silently.

## Frontend conventions

* **Routing: Waypoint.** Use the `staticRoute` helper (`Route.applyPF`) for
  static routes — `Route.static` with a `ClassTag` fails at runtime against
  Scala 3 enum singletons. Add a `Page` case + a route to `allRoutes` + a
  collector in `Main.pageViews`. Pages render through `SplitRender`, not a
  plain map over `currentPageSignal`, so a page whose URL params change (the
  board's `v`/`q` query params, see `Routes.homeRoute`) updates in place
  instead of being rebuilt — rebuilding refetches and drops input focus. State
  that should be shareable belongs in the URL: put it on the `Page` case and
  write it back with `router.replaceState`.
* **API: tapir-sttp-client.** Shared endpoints are body-only; cookies ride
  automatically (HttpOnly, browser-attached). Add a wrapper to `Api.scala`
  using `toRequestThrowDecodeFailures(Endpoints.x, baseUri = None)`.
* **Markdown** is stored raw and rendered through `marked` + `DOMPurify`
  (`Markdown.scala` facade) — never store or inject HTML. Use
  `Components.markdown(md)` (sets sanitised `innerHTML` in an `onMountCallback`).
* **Laminar gotchas:** structural tags are `sectionTag` / `headerTag` /
  `navTag`, not bare `section`/`header`/`nav` — use `div` unless you need the
  semantic tag. The `type` attribute is `tpe`. `onInput.mapToValue --> aVar`
  binds a `Var` directly. `emptyNode` for the empty branch.
* **Shared primitives** go in `ui/Components.scala` (badges, cards, the
  `Loaded` async-switch, markdown). Don't inline-then-extract.
* **Async load pattern:** `Var[Loaded[A]]`, fire the `Api` call in the page's
  `apply()`, `Components.loaded(state.signal)(view)`.

## Code style

Keep the `Repo`/`Service`/`Api` split. No section banners, no scaladoc on
trivial members.

## Writing docs and comments

Three rules, in this repo's markdown and its code alike:

* **Be concise.** Say it once, in the one place it belongs, and link rather
  than restate. A reader who has to skim is a reader who stops.
* **Drop history that is no longer relevant.** Docs describe how things are
  now, not how they got here — completed plans, superseded designs and
  migration notes for migrations that are done all come out. Git remembers.
* **Comment only what the code doesn't say itself.** Explain the *why* when it
  is non-obvious; never restate the *what*. One short summary line where a doc
  comment genuinely earns its place.

## Git

The repo is at `~/code/traits` (branch `main`). The convention: finish a
change, get `compile` green, let the user test, then one clean commit when
asked. Never amend/force-push. `frontend/package-lock.json` is tracked
(only `node_modules/` and `dist/` are ignored).
