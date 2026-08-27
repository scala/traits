package org.scalalang.traits.frontend.ui

import org.scalalang.traits.frontend.{Markdown, Page, Routes}
import org.scalalang.traits.shared.{
  Availability,
  AvailabilityStage,
  BoardColumn,
  Sip,
  SipState,
  VersionId
}
import com.raquo.laminar.api.L.*

import scala.math.Ordering.Implicits.infixOrderingOps

/** Three-state wrapper for an async load. */
enum Loaded[+A]:
  case Loading
  case Ok(value: A)
  case Failed(message: String)

object Components:

  def container(mods: Mod[HtmlElement]*): HtmlElement =
    div(cls := "max-w-5xl mx-auto px-4 py-6", mods)

  /** Wider variant for board/overview layouts that benefit from horizontal room. */
  def containerWide(mods: Mod[HtmlElement]*): HtmlElement =
    div(cls := "max-w-7xl mx-auto px-4 py-6", mods)

  def pageTitle(text: String): HtmlElement =
    h1(cls := "text-2xl font-semibold text-slate-900", text)

  def subtitle(text: String): HtmlElement =
    p(cls := "text-slate-500 text-sm mt-1", text)

  /** Internal SPA link that updates the Waypoint router and keeps a real href for cmd-click. */
  def pageLink(page: Page, text: String, extraCls: String = ""): HtmlElement =
    a(
      cls  := s"text-blue-600 hover:underline $extraCls",
      href := Routes.urlFor(page),
      onClick.preventDefault --> { _ => Routes.router.pushState(page) },
      text
    )

  def badge(text: String, colorCls: String): HtmlElement =
    span(
      cls := s"inline-block whitespace-nowrap text-xs font-medium px-2 py-0.5 rounded-full $colorCls",
      text
    )

  def columnClasses(column: BoardColumn): String = column match
    case BoardColumn.Idea         => "bg-slate-100 text-slate-600"
    case BoardColumn.PullRequest  => "bg-violet-100 text-violet-700"
    case BoardColumn.Experimental => "bg-orange-100 text-orange-700"
    case BoardColumn.Preview      => "bg-sky-100 text-sky-700"
    case BoardColumn.Stable       => "bg-emerald-100 text-emerald-700"
    case BoardColumn.Deprecated   => "bg-amber-100 text-amber-700"
    case BoardColumn.Removed      => "bg-rose-100 text-rose-700"

  // ---- board views (the pipeline and the SIP board) ----

  /** One stage of a board view: a stacked full-width section of cards, shown only when non-empty.
    */
  final case class BoardSection(
      label: String,
      colorCls: String,
      cards: List[HtmlElement]
  )

  /** One section per non-empty stage, in the given order. Each carries its own label and count, so
    * there is no separate index above them.
    */
  def board(sections: List[BoardSection]): HtmlElement =
    div(sections.filter(_.cards.nonEmpty).map(boardSection))

  private def boardSection(s: BoardSection): HtmlElement =
    div(
      cls := "mb-8",
      div(
        cls := "flex items-center gap-2 mb-3",
        badge(s.label, s.colorCls),
        span(cls := "text-xs text-slate-400", s.cards.size.toString)
      ),
      cardGrid(s.cards)
    )

  /** As many cards per row as fit at 16rem; four across the wide container. */
  def cardGrid(cards: List[HtmlElement]): HtmlElement =
    div(cls := "grid grid-cols-[repeat(auto-fill,minmax(16rem,1fr))] gap-2", cards)

  /** A board card: the title, then the SIP and where the feature is available — the availability
    * moves up to the second line when there is no SIP. Both lines keep their height when empty so
    * cards in a row line up; an idea or an open pull request has nothing to say about availability.
    */
  def boardCard(
      slug: String,
      cardTitle: String,
      tooltip: String,
      sip: Option[Sip],
      availability: Option[String]
  ): HtmlElement =
    val sipLine = sip.map(s => List(sipChip(s), metaText(SipState.label(s.state), inset = false)))
    val lines   = (sipLine.toList ++ availability.map(t => List(metaText(t))).toList).padTo(2, Nil)
    a(
      href := Routes.urlFor(Page.EntryView(slug)),
      onClick.preventDefault --> { _ => Routes.router.pushState(Page.EntryView(slug)) },
      cls := "block bg-white rounded-lg border border-slate-200 px-3 py-2 hover:border-blue-300 hover:shadow-sm transition",
      title := tooltip,
      div(cls := "font-medium text-slate-900 text-sm truncate", cardTitle),
      lines.take(2).map(l => div(cls := "flex items-center gap-1.5 h-5 mt-0.5", l))
    )

  private def sipChip(s: Sip): HtmlElement =
    span(
      cls := "shrink-0 font-mono text-xs px-1.5 rounded bg-indigo-50 text-indigo-600",
      s.number.getOrElse("SIP")
    )

  /** `inset` pads a line's text by exactly the chip's own padding, so every line of a card starts
    * on the same vertical line whether or not it opens with the chip.
    */
  private def metaText(text: String, inset: Boolean = true): HtmlElement =
    span(cls := s"truncate text-xs text-slate-500${if inset then " pl-1.5" else ""}", text)

  /** The furthest point reached on the availability track, for a card with no version of its own to
    * anchor to. `latest` is the newest released version, which decides "since" against "in".
    */
  def reachedText(availability: List[Availability], latest: Option[VersionId]): Option[String] =
    availability
      .filterNot(_.backport)
      .filter(_.version.isDefined)
      .maxByOption(_.version)
      .map(a => statusText(a, upcoming = !a.version.exists(v => latest.exists(v <= _))))

  /** Short "where it stands" line for an availability entry: "Stable since 3.8", "Removed in 3.11
    * (backport)", "Pull request open". `upcoming` is for a version the feature has not reached yet,
    * where "since" would read backwards.
    */
  def statusText(a: Availability, upcoming: Boolean = false): String =
    a.version match
      case None => "Pull request open"
      case Some(v) =>
        val preposition = if upcoming || a.stage == AvailabilityStage.Removed then "in" else "since"
        val backport    = if a.backport then " (backport)" else ""
        s"${AvailabilityStage.label(a.stage)} $preposition ${v.render}$backport"

  /** Render trusted-after-sanitising markdown into a styled block. */
  def markdown(md: String): HtmlElement =
    div(
      cls := "rich-content text-slate-700 text-sm leading-relaxed",
      onMountCallback(ctx => ctx.thisNode.ref.innerHTML = Markdown.render(md))
    )

  def spinner: HtmlElement =
    div(cls := "text-slate-400 text-sm py-10 text-center", "Loading…")

  def errorBox(message: String): HtmlElement =
    div(cls := "bg-rose-50 text-rose-700 text-sm rounded-md px-4 py-3", message)

  /** Standard async-content switch used by every page. */
  def loaded[A](signal: Signal[Loaded[A]])(view: A => HtmlElement): HtmlElement =
    div(
      child <-- signal.map {
        case Loaded.Loading   => spinner
        case Loaded.Failed(m) => errorBox(m)
        case Loaded.Ok(value) => view(value)
      }
    )

  // ---- form primitives (editor UI) ----

  val btnPrimary =
    "bg-blue-600 text-white rounded-md px-4 py-2 text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
  val btnSecondary =
    "border border-slate-300 text-slate-700 rounded-md px-4 py-2 text-sm hover:bg-slate-50"
  val btnDanger =
    "border border-rose-300 text-rose-700 rounded-md px-4 py-2 text-sm hover:bg-rose-50"

  /** Board-header variant of `controlCls`: sized to its content instead of filling a column. */
  val boardControlCls =
    "border border-slate-300 rounded-md px-3 py-1.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-200"

  /** The filter box both boards carry. The term lives in the page's URL query, so it is passed in
    * as a signal and written back through `set` rather than held here.
    */
  def filterInput(
      term: Signal[String],
      set: Option[String] => Unit,
      placeholderText: String
  ): HtmlElement =
    input(
      tpe         := "search",
      placeholder := placeholderText,
      cls         := s"$boardControlCls w-56",
      controlled(
        value <-- term,
        onInput.mapToValue --> { s => set(Some(s).filter(_.nonEmpty)) }
      )
    )

  private val controlCls =
    "w-full border border-slate-300 rounded-md px-3 py-1.5 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-blue-200"

  def fieldLabel(text: String): HtmlElement =
    div(cls := "text-xs font-medium uppercase tracking-wide text-slate-500 mb-1", text)

  def field(labelText: String, control: HtmlElement): HtmlElement =
    label(cls := "block", fieldLabel(labelText), control)

  def textInput(v: Var[String], placeholderText: String = ""): HtmlElement =
    input(
      tpe         := "text",
      placeholder := placeholderText,
      cls         := controlCls,
      controlled(value <-- v.signal, onInput.mapToValue --> v)
    )

  def passwordInput(v: Var[String], placeholderText: String, onEnter: () => Unit): HtmlElement =
    input(
      tpe         := "password",
      placeholder := placeholderText,
      cls         := controlCls,
      controlled(value <-- v.signal, onInput.mapToValue --> v),
      onKeyDown.filter(_.key == "Enter") --> { _ => onEnter() }
    )

  def multilineInput(v: Var[String], rowCount: Int = 4, placeholderText: String = ""): HtmlElement =
    textArea(
      rows        := rowCount,
      placeholder := placeholderText,
      cls         := s"$controlCls font-mono leading-relaxed",
      controlled(value <-- v.signal, onInput.mapToValue --> v)
    )

  def checkboxInput(v: Var[Boolean]): HtmlElement =
    input(
      tpe := "checkbox",
      cls := "h-4 w-4 rounded border-slate-300 align-middle",
      controlled(checked <-- v.signal, onInput.mapToChecked --> v)
    )

  /** A single-select bound to a typed `Var[A]`. `key` must be a stable, unique string per option.
    */
  def selectInput[A](
      current: Var[A],
      options: List[A],
      render: A => String,
      key: A => String
  ): HtmlElement =
    select(
      cls := controlCls,
      controlled(
        value <-- current.signal.map(key),
        onChange.mapToValue.map(s =>
          options.find(o => key(o) == s).getOrElse(options.head)
        ) --> current
      ),
      options.map(o => option(value := key(o), render(o)))
    )
