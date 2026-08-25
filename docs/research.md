# Sources for an entry

Before adding or updating an entry, get each of these — or ask the user for it.
Say which ones don't exist for this change; never skip one silently.

**SIP PR** — `github.com/scala/improvement-proposals/pulls`, closed ones
included, since rejected and withdrawn proposals are closed. Its labels give
`sip.state`, the URL is a `Pr` link, and votes or changes of direction are dated
`timeline` events.

**Published SIP** — `docs.scala-lang.org/sips/<number>.html`, indexed at
`all.html`. Gives `sip.url` and the `Sip` link.

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

Then:

* PR labels beat prose for `sip.state`.
* `timeline` is for events that are *not* stage transitions — those are
  `availability` rows, and duplicating them will eventually contradict.
* `watch: true` on the SIP PR and any live thread; not on shipped release notes.
* Summarise discussions in prose. Never quote non-public notes.

The SIP page URL comes from `number` in the file's frontmatter, not its
filename. Unmerged proposals get a frontmatter-only stub named after the PR
title, so a rename moves the URL. Both regenerate daily, so state can lag a day.
