# Sources for an entry

Before adding or updating an entry, get each of these — or ask the user for it.
Say which ones don't exist for this change; never skip one silently.

**SIP PR** — `github.com/scala/improvement-proposals/pulls`, closed ones
included, since rejected and withdrawn proposals are closed. Its labels give
`sip.state`, the URL is a `Pr` link, and votes or changes of direction are dated
`timeline` events. Read the comments too: they are public, and a manager's
requirements or an author's "this already shipped in 3.5" often settle what the
entry should say.

**The proposal document** — the `.md` the PR adds or changes, or the merged one
under `content/`. Its summary is the entry's Overview and its History table
gives dated drafts and revisions; it also cites the issues and PRs the proposal
rests on. For an unmerged PR the file exists only in the diff — `raw_url` comes
back empty, so fetch `contents_url` from
`gh api repos/scala/improvement-proposals/pulls/<n>/files`. Its frontmatter may
name the forum thread in `presip-thread`, which saves a search when it isn't the
`pre-sip-foo-bar/9999` placeholder.

**PR label history** — the `labeled`/`unlabeled` events from `gh api
repos/scala/improvement-proposals/issues/<n>/timeline`. This dates every state
transition, including votes whose meeting notes you can't reach, and it is the
reliable date for acceptance: a proposal is accepted when `status:under-review`
gives way to `status:waiting-for-implementation`, often months before the
document merges. Don't read the merge date as the acceptance date.

**Published SIP** — `docs.scala-lang.org/sips/<number>.html`, indexed at
`all.html`. For a completed proposal this is `sip.url`; while one is still
unmerged its page is only a stub, so use the PR URL until it lands.

**Forum threads** — search `contributors.scala-lang.org/search.json?q=…`, then
read `/t/<id>.json`. A change often has several: a pre-SIP discussion, a design
debate, an announcement. Each is a `ForumThread` link; what they contain becomes
a prose summary.

**Release notes** — `github.com/scala/scala3/releases` and
`scala-lang.org/blog`. Expect one per stage change rather than just the first,
each confirming the version on an `availability` row.

**Meeting notes** — `docs.scala-lang.org/sips/meeting-results.html` is public
but incomplete. **Ask the user** for the rest; they are not public. Vote
outcomes become `timeline` events.

**Implementation PRs** — `github.com/scala/scala3/pulls`. The evidence for
`availability` rows, and backport PRs are the only source for `backport: true`.
A PR's **milestone** dates the stage directly, when it has one.

**How the feature is gated** — `library/src/scala/language.scala` and
`library/src/scala/runtime/stdLibPatches/language.scala` hold the
`language.experimental.*` objects, `compiler/.../config/ScalaSettings.scala` the
`-Y` flags, and library types carry `@experimental`. This is what the
`availability` note should say, and often the answer to whether a feature is
experimental at all.

**Which release actually has it** — fetch the same file at a tag or branch
(`?ref=3.9.0-RC6`, `?ref=main`, `?ref=3.3.0`) and see whether the flag is there.
Many PRs carry no milestone, and a PR merged after a release branch was cut
lands in the *next* minor, not the one in progress. Check rather than infer:
this is what separates a verified `availability` row from a plausible one.

Then:

* PR labels beat prose for `sip.state`.
* `timeline` is for events that are *not* stage transitions — those are
  `availability` rows, and duplicating them will eventually contradict.
* `watch: true` on the SIP PR and any live thread; not on shipped release notes.
* Summarise discussions in prose. Never quote non-public notes.
* **Record decisions, not sentiment.** A vote, a deferral, a change of
  direction belongs in the `timeline`; which way a discussion seemed to be
  leaning does not. Publishing a predicted outcome ahead of the vote is unfair
  to the proposer and is not something the committee has decided.

The SIP page URL comes from `number` in the file's frontmatter, not its
filename. Unmerged proposals get a frontmatter-only stub named after the PR
title, so a rename moves the URL. Both regenerate daily, so state can lag a day.
