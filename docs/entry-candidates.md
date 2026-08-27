# Candidate entries

What is not in the database, and why. Two parts: things to add or consider, and
things looked at and set aside — including whole categories that are ruled out
on principle, so the next sweep does not rediscover them.

Nothing here is researched: a candidate may turn out to be a duplicate of an
entry we already have, an abandoned experiment, two things filed as one, or
nothing at all — that surfaces when it is investigated. Nothing has been tested
against the bar in [`PLAN.md`](../PLAN.md#what-earns-an-entry) either. Whole
groups below — the lint flags, the REPL and tooling changes, the packaging
changes — will mostly fail it. They are listed so that the test gets applied on
purpose rather than by omission.

Sources swept:

- `scala/improvement-proposals` — open PRs, closed-and-merged, and
  closed-unmerged; the SIP index at `docs.scala-lang.org/sips/all.html`.
- `Feature.scala` and `stdLibPatches/language.scala` — the flag inventory.
- `docs/sidebar.yml` and `docs/_docs/reference/` — the documented features.
- The `Highlights of the release` section of every
  [scala3 release](https://github.com/scala/scala3/releases).
- `scala-lang.org/news/<version>` — the release announcements, 3.2.2 to 3.8.4.
  These carry the minor releases from 3.6 on.
- Release posts on [the blog](https://www.scala-lang.org/blog/) — the minor
  releases up to 3.5.0 (August 2024), after which announcements moved to
  `news/`.

3.9.0 has neither a news page nor a blog post; its release notes on GitHub are
the only source.

---

## Part 1 — To add, or to consider

A row beginning with `—` belongs to the feature above it: one candidate entry,
several things to research.

### Rejected and withdrawn proposals

Every open and every completed Scala 3 era SIP has an entry. The closed ones do
not, though `SipState` models Rejected and Withdrawn as terminal states and
`research.md` says to search closed PRs for exactly this reason. Labels and
dates are read from the PR.

| Proposal | State | Closed | Source |
| --- | --- | --- | --- |
| SIP-78 — Relaxed De-dented Case Syntax | Rejected | 2026-07-05 | PR #129, `manager:bjornregnell` |
| One-element tuple syntax (never numbered) | Withdrawn | 2025-12-31 | PR #121, labelled `scala 4` |
| SIP-55 — Concurrency with Higher-Order Coroutines | Withdrawn | 2025-03-21 | PR #63, `stage:design` |
| SIP-48 — Precise Type Modifier | Withdrawn | 2023-09-14 | PR #48, `stage:design` |
| SIP-43 — Pattern matching with named fields | Withdrawn | 2023-10-24 | PR #44, `stage:design`; superseded by SIP-58 |
| SIP-52 — Wildcard context bounds | Withdrawn | 2023-01-17 | PR #55; the number was later reused for Binary APIs |
| SIP-50 — Struct Classes | Withdrawn | 2022-12-21 | PR #50 |

### Flag-gated language features

Each has a language import or a compiler flag, so each has a ready
`availability` note, and each is experimental unless the table says otherwise.

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| Safer exceptions | Experimental | 3.1 | `experimental.saferExceptions`, `experimental/canthrow.md` |
| Erased definitions | Experimental |  | `experimental.erasedDefinitions`, `experimental/erased-defs.md` |
| Named type arguments | Experimental |  | `experimental.namedTypeArguments`, `experimental/named-typeargs.md` |
| Dependent method types | Experimental |  | `experimental.dependent`; commented out in `language.scala`. Not the dependent case classes of 3.7, below |
| Explicit nulls | Experimental |  | `-Yexplicit-nulls`, `language.unsafeNulls`, `experimental/explicit-nulls.md` |
| — null annotations in the standard library | Experimental | 3.8 | #23566, `news/3.8` |
| Modularity improvements | Experimental |  | `experimental.modularity`, `experimental/modularity.md`, `typeclasses.md` |
| — applied constructor types | Experimental | 3.7.2 | #22543, `news/3.7.2` |
| — `tracked` members | Experimental | 3.6.4 | #21761; the release notes file this under capture checking |
| Separation checking | Experimental | 3.8 | `experimental.separationChecking`, `news/3.8`; may belong to `capture-checking` |
| Quoted patterns with polymorphic functions | Experimental |  | `experimental.quotedPatternsWithPolymorphicFunctions` |
| `magic` — extensions for working with coding agents | Experimental |  | `Feature.scala`; no doc page, no release note |
| `TupledFunction` | Experimental |  | `experimental/tupled-function.md` |
| `@main` annotation | Experimental |  | `experimental/main-annotation.md`; distinct from the `@main` methods of 3.0 |
| Spec strings | Experimental |  | `experimental/spec-strings.md` |
| Scala 2 macro support | Experimental |  | `experimental.macros`; keeps a dropped feature reachable |
| Symbol literals | Deprecated |  | `language.deprecated.symbolLiterals`, `dropped-features/symlits.md` |

### The experimental and preview machinery

Not features but the mechanism the availability track models: how a definition
is marked, and how a user opts in.

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| `-experimental` flag and `@experimental` | Stable | 3.4 | `other-new-features/experimental-defs.md`, #18571 |
| `-preview` flag and `@preview` | Stable | 3.7 | `other-new-features/preview-defs.md`, #22317 |

### Language and syntax

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| `export` in extension clauses | Stable | 3.2 | blog 3.2.0 |
| `given` bindings in for-comprehensions | Stable | 3.2 | blog 3.2.0 |
| Class constructors with `using` clauses | Stable | 3.2 | blog 3.2.0 |
| `boundary` and `break` | Stable | 3.3 | #16612, blog 3.3.0 |
| Refutable patterns in for-comprehensions require `case` | Stable | 3.4 | #18842, blog 3.4.0 |
| `var` in refinements | Stable | 3.5 | #19982, blog 3.5.0 |
| Dependent case classes | Stable | 3.7 | #21698, `news/3.7.0` |
| Annotations may annotate themselves | Stable | 3.8 | #24447, `news/3.8` |

### Type system and inference

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| `Mirror` synthesis | Stable | 3.1 | blog 3.1.0; mirrors for hierarchical sealed types |
| — local and inner classes, generic tuples | Stable | 3.2 | blog 3.2.0 |
| `Manifest` synthesis for Scala 2 compatibility | Stable | 3.1 | blog 3.1.0 |
| Inference for fold-like functions | Stable | 3.4 | #18780, blog 3.4.0 |
| Value parameter inference for polymorphic lambdas | Stable | 3.4 | #18041, blog 3.4.0 |
| Looping given definitions no longer generated | Stable | 3.4 | #19282, blog 3.4.0 |
| Context bounds map to using clauses | Stable | 3.6 | #21257; a desugaring change, tied to implicit priority |
| New scheme for given prioritization | Stable | 3.7 | `news/3.6.2`, `news/3.7.0` |

### Java interoperability

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| Java records in Java dependencies | Stable | 3.3.1 | `news/3.3.1` |
| JEP-409 sealed classes in mixed codebases | Stable | 3.3.4 | `news/3.3.4` |
| Named arguments required for Java-defined annotations | Stable | 3.6 | #21329, `news/3.6.2` |

### Metaprogramming

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| `-Xmacro-settings` | Experimental | 3.1.2 | blog 3.1.2 |
| Arbitrary class implementations in macros | Stable | 3.1.3 | blog 3.1.3 |
| Quotes API additions — `newClass`, `summonIgnoring`, import selectors | Experimental | 3.4, 3.7 | `news/3.7.0`, release notes 3.4.0; a stream of small additions, probably not one entry |

### Standard library

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| `@deprecatedInheritance` | Stable | 3.4.1 | `news/3.4.1` |
| `Tuple.Filter`, infix `Tuple.Append` / `Concat` | Stable | 3.6 | #21286, #21288 |
| `@uncheckedOverride` | Stable | 3.3.8 | #24545, `news/3.3.8` |

### Linting and warnings

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| `-Wconf` and `@nowarn` | Stable | 3.1 | blog 3.1.0 |
| `-Wunused` | Stable | 3.3 | #16157, blog 3.3.0 |
| `-Wvalue-discard` | Stable | 3.3 | #15975, blog 3.3.0 |
| `-Wnonunit-statement` | Stable | 3.3.1 | `news/3.3.1` |
| `-Xlint:private-shadow`, `-Xlint:type-parameter-shadow` | Stable | 3.3.4 | `news/3.3.4` |
| `-Wall` | Stable | 3.5.2 | #20577, `news/3.5.2` |
| `-Wconf` origin filter | Stable | 3.5.2 | #21404, `news/3.5.2` |
| Implicit parameters warn at the call site without `using` | Stable | 3.7 | #22441, `news/3.7.0` |
| Warn when an interpolator calls `toString` | Stable | 3.7.1 | #20578, `news/3.7.1` |
| Warn when an implicit default shadows a given | Stable | 3.7.3 | #23559, `news/3.7.3` |
| Warn on `for` with many vals and an overloaded `map` | Stable | 3.8.2 | #25090, `news/3.8.2` |

### Compilation model and compiler flags

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| Efficient bytecode for matching over `String` | Stable | 3.1 | blog 3.1.0 |
| `-scala-output-version`, also seen as `-Yscala-release` and `-Xscala-release` | Removed | added 3.1.2, removed 3.2 | blog 3.1.2; dropped in favour of LTS, and absent from `ScalaSettings.scala` today — a complete lifecycle |
| `-release` renamed to `-java-output-version` | Stable | 3.1.2 | blog 3.1.2, `ScalaSettings.scala` |
| Native code coverage — `-coverage-out` | Stable | 3.2 | blog 3.2.0 |
| — coverage filter options | Stable | 3.3.4 | `news/3.3.4` |
| — local exclusions `// $COVERAGE-OFF$` | Stable | 3.8.3 | #24486, `news/3.8.3` |
| `-Vprofile` | Stable | 3.2 | blog 3.2.0 |
| Lazy vals — a new initialization scheme | Stable | 3.3 | #16614, blog 3.3.0 |
| — `-Ylightweight-lazy-vals`, the opt-in first cut | Experimental | 3.2.2 | `news/3.2.2` |
| — reimplemented on `VarHandle`, replacing `sun.misc.Unsafe` | Stable | 3.8 | `news/3.8` |
| — `-Yfuture-lazy-vals`, the same for JDK 9+ on the LTS line | Experimental | 3.3.8 | `news/3.3.8` |
| Parallel JVM backend — `-Ybackend-parallelism` | Stable | 3.4 | #15392, blog 3.4.0 |
| Polymorphic lambdas compiled to JVM lambdas | Stable | 3.4 | #17548, blog 3.4.0 |
| Best-effort compilation | Stable | 3.5 | #17582, blog 3.5.0 |
| Pipelined builds | Stable | 3.5 | #18880, blog 3.5.0 |
| Annotation arguments no longer lifted | Stable | 3.6.4 | #22035, `news/3.6.4` |
| `@implicitNotFound` / `@implicitAmbiguous` require string literals | Stable | 3.6.4 | #22371, `news/3.6.4` |
| `-Yimplicit-to-given` rewrite flag | Experimental | 3.7.2 | #22580, `news/3.7.2` |

### Tooling, runner and REPL

What a user meets in the tools. Where the tools themselves were repackaged, the
row is under packaging instead.

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| `scala -e` | Stable | 3.1.2 | blog 3.1.2 |
| Code completion for refined `Selectable` types | Stable | 3.2 | blog 3.2.0 |
| Compilation progress reporting and cancellation | Stable | 3.4 | #18739, blog 3.4.0 |
| Diagnostics exported to SemanticDB | Stable | 3.4 | #17835, blog 3.4.0 |
| `-language:help` | Stable | 3.5.1 | `news/3.5.1` |
| REPL `:silent`, `--repl-init-script` | Stable | 3.6.4 | #22248, #22206, `news/3.6.4` |
| REPL `:jar`, deprecating `:require` | Stable | 3.7 | #22343, `news/3.7.0` |
| `:help` for all compiler settings | Stable | 3.8.4 | #26052, `news/3.8.4` |

### Platform, packaging and binary compatibility

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| The LTS series | Stable | 3.3 | blog 3.3.0, `long-term-compatibility-plans` |
| LTS/Next indicator in `pom.xml` | Stable | 3.3, attribute renamed 3.8 | #24709, `news/3.8` |
| TASTy attributes section | Stable | 3.4 | #18599, #18948, blog 3.4.0 |
| TASTy version error messages | Stable | 3.4 | #18828, blog 3.4.0 |
| Expression Compiler folded into the compiler | Stable | 3.7 | #22597, `news/3.7.0` |
| Scala 3 unblocked on Android | Stable | 3.7 | #22632, `news/3.7.0` |
| Nightlies published to `repo.scala-lang.org` | Stable | 3.7.3 | `news/3.7.3` |
| REPL extracted to its own artifact | Stable | 3.8 | #24243, `news/3.8` |
| `Automatic-Module-Name` restored in the stdlib manifest | Stable | 3.9 | #26218, release notes 3.9.0 |
| Scala CLI's using-directive parser moved into the compiler | Stable | 3.9 | #26449, release notes 3.9.0 |
| Support for each new JDK — 22, 23, 24, 25, 26 | Stable | 3.3.4, 3.5.2, 3.6.4, 3.7.1, 3.8 | news pages; a per-release fact, and not the same thing as raising the *minimum* JDK |

### Deprecations and removals since 3.0

Cuts across the categories above; collected here because the deprecated and
removed stages are otherwise the emptiest part of the database.

| Feature | State | Version | Source |
| --- | --- | --- | --- |
| Legacy syntax — `_` wildcards, `private[this]`, `var x = _`, `with` as a type operator, `xs: _*`, trailing `_` | Deprecated | 3.4 | blog 3.4.0; one entry or six, to decide |
| `-Xno-decode-stacktraces`, now an alias for `-Xno-enrich-error-messages` | Deprecated | 3.6.4 | #22208, `news/3.6.4` |
| `scala_legacy`, `MainGenericRunner`, `scalac -run`, `scalac -repl` | Removed | deprecated 3.5, removed 3.8 | #24267, `news/3.7.4` |
| `-print-lines`, deprecated for removal but accepted as a no-op | Deprecated | 3.8.3 | #25330, `news/3.8.3` |
| Automatic collection of entry points | Removed | 3.9 | #25215, release notes 3.9.0 |


---

## Part 2 — Considered and not added

Two reasons, and they are not the same. The first is out *for now*, pending the
scope boundary decision in [`sip-findings.md`](sip-findings.md); the second is
out *by rule*, whatever that decision turns out to be.

The boundary sections split by which language the work belongs to, because that
is what the decision turns on. Note that *old SIP process* and *Scala 2* are not
the same thing: several proposals made under the old process were implemented in
Scala 3.0.0 and nowhere else. Where a proposal document states where it was
implemented, that statement is quoted rather than inferred.

### Scala 3.0.0 — pending the scope boundary

The seven completed proposals whose feature shipped in Scala 3.0.0 — trait
parameters, `@static`, by-name implicits, opaque types, quote escapes,
the partial-function converters and right-associative by-name operators — have
been added, on the grounds that `PLAN.md` says a SIP always qualifies. What is
left below has not.

**Rejected or withdrawn proposals that targeted Scala 3** — only one: submitted
weeks after 3.0.0 shipped, and written against `TypeTest`. Nothing was
implemented, so the argument that carried the seven does not carry this one.

| PR | Proposal | State | Drafted |
| --- | --- | --- | --- |
| #43 | SIP-41 — Sealed Types | Withdrawn | 2021-05 |

**Features that shipped in 3.0.0** — the reference taxonomy as it stood at the
release, documented under `docs/_docs/reference/`. Listed so that individual
ones can be argued for rather than silently missed. Trait parameters and opaque
type aliases have been removed from the list: they now have entries. One
deliberate overlap remains — Scala 2 macros and symbol literals appear here as
dropped features and in Part 1 as flags that still reach them. Match types are
covered by the existing SIP-56 entry, which specified them in 3.4.

| Group | Features |
| --- | --- |
| New types | Intersection types · union types · type lambdas · match types · dependent function types · polymorphic function types |
| Enums | Enums · algebraic data types |
| Contextual abstractions | Givens · using clauses · context bounds · deferred givens · given imports · extension methods · right-associative extension methods · type class derivation · multiversal equality · context functions · implicit conversions · by-name context parameters |
| Metaprogramming | `inline` · `compiletime` operations · quotes and splices · staging · reflection · TASTy inspection |
| Other new features | Transparent traits · creator applications · `export` · open classes · parameter untupling · kind polymorphism · `Matchable` · `@threadUnsafe` · `@targetName` · optional braces and control syntax · safe initialization · type tests · top-level definitions |
| Changed features | Numeric literals · structural types · operators and `infix` · wildcards · imports · type inference · implicit resolution · implicit conversions · overload resolution · match syntax · vararg splices · pattern bindings · pattern matching · eta expansion · compiler plugins · `@main` methods · interpolation escapes |
| Dropped features | `DelayedInit` · Scala 2 macros · existential types · type projections · `do..while` · procedure syntax · package objects · early initializers · class shadowing · the 22 limit · XML literals · symbol literals · auto-application · weak conformance · non-local returns · `this` qualifier · wildcard initializers |

### Scala 2 — pending the scope boundary

Nothing here reached Scala 3 under its own name, so these fall the moment the
boundary is drawn anywhere after 3.0.0.

**Completed proposals with no Scala 3 statement in the document** — the SIDs of
the 2.8 era and the proposals that predate the Dotty work. Some of their
subjects survive in Scala 3 anyway; none of the documents says so.

| Proposal | Title | Implemented in |
| --- | --- | --- |
| SID-1 | Named and Default Arguments | — |
| SID-2 | Compiler Phase and Plug-In Initialization | — |
| SID-3 | New Collection classes | — |
| SID-4 | Early Member Definitions | — |
| SID-5 | Internals of Scala Annotations | — |
| SID-7 | Scala 2.8 Arrays | — |
| SID-8 | Scala Swing Overview | — |
| SID-9 | Scala Specialization | — |
| SID-10 | Storage of pickled Scala signatures in class files | — |
| SIP-11 | String Interpolation | — |
| SIP-13 | Implicit classes | — |
| SIP-14 | Futures and Promises | — |
| SIP-15 | Value Classes | — |
| SIP-17 | Type Dynamic | — |
| SIP-18 | Modularizing Language Features | — |
| SIP-23 | Literal-based singleton types | the body cites Typelevel Scala and Dotty |
| SIP-27 | Trailing Commas | Scala 2.12.2 |
| SIP-33 | Priority-based infix type precedence | — |

**Rejected or withdrawn proposals** — migrated into
`scala/improvement-proposals` in the 2022-06-09 batch and closed without
merging. *Drafted* is the first date in the document's own history table, blank
where it keeps none.

| PR | Proposal | State | Drafted | Note |
| --- | --- | --- | --- | --- |
| #12 | SIP-12 — Uncluttering Scala's syntax for control structures | Rejected | — |  |
| #15 | SIP-16 — Self-cleaning Macros | Rejected | — |  |
| #18 | SIP-19 — Implicit Source Locations | Rejected | — |  |
| #19 | SIP-20 — Improved Lazy Vals Initialization | — | — | labelled `stage:completed`, `status:shipped` on a closed, unmerged PR: the labels disagree with the outcome |
| #20 | SIP-21 — Spores | Withdrawn | — |  |
| #21 | SIP-22 — Async | Withdrawn | — |  |
| #23 | SIP-24 — Repeated By Name Parameters | Withdrawn | — |  |
| #27 | SIP-26 — Unsigned Integers | Rejected | 2015-11 |  |
| #28 | SIP-28 — Inline meta | Withdrawn | 2016-08 |  |
| #29 | SIP-32 — Referring to other arguments in default parameters | Rejected | 2017-01 | labelled `scala 4` |
| #30 | SIP-34 — Improving binary compatibility with `@stableABI` | Withdrawn | 2017-01 | dormant; the ancestor of SIP-52, which has an entry |
| #32 | Comonadic comprehensions (never numbered) | Rejected | 2017-02 |  |
| #35 | SIP-36 — Adding prefix types | Withdrawn | 2017-10 |  |
| #37 | SIP-39 — Uncluttering Abuse of Match | Rejected | 2018-05 | the number was reused for Right-Associative By-Name Operators |
| #41 | SIP-45 — Curried varargs | Rejected | 2019-08 |  |
| #42 | SIP-40 — Name Based XML Literals | Rejected | 2019-10 |  |


### Out by rule

Ruled out by kind, not case by case, and not affected by the boundary decision.
Each is a large, recurring source that a sweep will otherwise walk into again.

Part 1 does still list a few individual items that fall under these rules — the
performance work and the `-Y` flags that were named as release highlights. They
are kept there so that the call is made on the item rather than by omission.

| Kind | Why | Where it turns up |
| --- | --- | --- |
| Scala 2 releases and 2.13.x changes | Traits tracks Scala 3 | `scala/scala` releases, `news/2.13.x` |
| Bug fixes, regressions and post-mortems | `PLAN.md` excludes crash fixes | `Other changes and fixes` in every release |
| Dependency bumps — Scala.js, Scala CLI, Scala 2 stdlib, asm | Not changes to Scala | release highlights, almost every patch |
| Error messages and the error code pages | `PLAN.md` excludes error-message wording | `reference/error-codes/`, E001–E188 |
| Performance work with no change to what compiles | `PLAN.md` excludes performance work | release highlights |
| `-Y` flags | `PLAN.md` excludes them — except where a `-Y` flag is the only gate on a real feature, as with explicit nulls | `ScalaSettings.scala` |
| Presentation compiler, Metals and IDE changes | Tooling around the language, not the language | release highlights |
| LTS backports | Recorded as `backport: true` on the entry they belong to, never as their own entry | `news/3.3.x` |
| Non-release blog posts — Scala Days, GSoC, sbt, security advisories, surveys | Not Scala language changes | the blog |
| Reference pages that describe no feature | `syntax.md`, `soft-modifier.md`, `features-classification.md`, `language-versions/` | `docs/_docs/reference/` |
