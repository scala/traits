# Findings from curating the SIPs

All 19 open proposals have entries, as do the 19 completed ones from the Scala 3
era. Only what still needs doing is kept below.

## Needs a decision here

- **The scope boundary is not written down.** `PLAN.md` says a feature that
  shipped in 3.0.0 is out of scope "unless they have a SIP, which always
  qualifies" — which, read literally, admits SIP-1 *Named and Default Arguments*
  from Scala 2.8. The 25 completed Scala 2 era SIPs were left out by judgement,
  not by a rule, so the next person to work the list will follow the wording and
  add them. The bound meant was something like "the SIP is active, or its
  subject has moved since 3.0.0".

## Worth raising with the SIP officers

- **SIP-70 has no page on docs.scala-lang.** Its label is `status:implemented`,
  which is not one of the eight statuses the process defines, so the docs
  generator cannot decode its state and skips it — `/sips/flexible-varargs.html`
  is a 404 while every sibling resolves. Setting `status:under-review` would fix
  the page and match what is true — SIP-71 carried the same label for three
  weeks in 2025 and was corrected that way.
- **Two other non-standard values.** SIP-77's frontmatter says
  `status: proposal`; SIP-63 sits in a "dormant" state — an earlier vote
  declined to accept it without rejecting it — which has no label and no place
  in the process spec. Worth deciding whether dormant should be a real state.
- **Template defaults survive into submitted proposals.** Most unmerged
  proposals carry `stage: implementation`, `status: waiting-for-implementation`
  and `presip-thread: …/pre-sip-foo-bar/9999` verbatim from `sip-template.md`,
  contradicting their labels. Harmless for us — while a PR is unmerged the
  generator uses the labels, so we do too — but a lint would help authors.
- **The sub-cases reference page names the wrong import.**
  `reference/experimental/sub-cases.html` says the feature is enabled by
  `fewerBraces`; the compiler says `subCases`.

## To recheck

- **The 2026-08-28 meeting** has #137, #138 and SIP-72 on its agenda under
  Decisions. All three entries have `watch: true` set.
- **SIP-63 (`macro-annotations`) may be live again.** It was parked to see
  whether inline traits could cover the same ground; that work merged on
  2026-08-15, and an August 2025 review had already concluded inline traits are
  not sufficient.
- **Two `sip.state` values we are not confident in.** #138 has no labels at all,
  so its state is inferred; #135 is labelled `submitted` but the author returned
  it to draft and pulled it from the July agenda, so the label overstates where
  it is. Revisit both once the labels catch up.

## Gaps in what we can see

- **Meeting notes reach back only to 2024-05-24** — the chain of "previous
  meeting" links ends there, so older votes are visible only through PR label
  history. Recovering the older URLs is being looked into.
- **SIP-65 carries no `manager:` label**, though a manager has been acting on
  the PR since June 2024 — so the labels can understate who is involved.
