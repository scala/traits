package org.scalalang.traits.frontend.pages

import org.scalalang.traits.frontend.ui.{Components, Loaded}
import org.scalalang.traits.frontend.{Api, Page, Routes}
import org.scalalang.traits.frontend.Api.given
import org.scalalang.traits.shared.*
import com.raquo.laminar.api.L.*

import scala.math.Ordering.Implicits.infixOrderingOps

/** Home view: the pipeline, one stacked section per availability stage plus Idea, computed for a
  * picked version (default: the latest released one). The picked version and the text filter are
  * URL query params (`/?v=3.4&q=tuple`) — the URL is the source of truth, written with
  * `replaceState` so typing doesn't pollute history.
  */
object BoardPage:

  def apply(pageSignal: Signal[Page.Home]): HtmlElement =
    val state = Var[Loaded[(List[EntrySummary], List[Version])]](Loaded.Loading)

    Api.listEntries().zip(Api.listVersions()).foreach {
      case (Right(es), Right(vs)) => state.set(Loaded.Ok((es, vs)))
      case (Left(e), _)           => state.set(Loaded.Failed(e.message))
      case (_, Left(e))           => state.set(Loaded.Failed(e.message))
    }

    def update(f: Page.Home => Page.Home): Unit =
      Routes.router.currentPageSignal.now() match
        case h: Page.Home => Routes.router.replaceState(f(h))
        case _            => ()

    Components.containerWide(
      Components.loaded(state.signal) { case (entries, versions) =>
        val released = versions.filter(_.released).map(_.version).toSet
        val default = Version
          .latestReleased(versions)
          .orElse(versions.map(_.version).maxOption)
          .getOrElse(VersionId(3, 0))
        def versionOf(p: Page.Home): VersionId =
          p.version.flatMap(VersionId.parse).getOrElse(default)

        div(
          div(
            cls := "flex flex-wrap items-end justify-between gap-3 mb-5",
            Components.pageTitle("Scala language features"),
            div(
              cls := "flex items-end gap-3",
              if versions.isEmpty then
                span(cls := "text-xs text-amber-600", "Version registry is empty")
              else
                label(
                  cls := "block",
                  Components.fieldLabel("Scala version"),
                  select(
                    cls := Components.boardControlCls,
                    controlled(
                      value <-- pageSignal.map(p => versionOf(p).render),
                      onChange.mapToValue --> { s => update(_.copy(version = Some(s))) }
                    ),
                    versions
                      .map(_.version)
                      .sorted
                      .reverse
                      .map(v => option(value := v.render, versionLabel(v, versions)))
                  )
                )
              ,
              Components.filterInput(
                term = pageSignal.map(_.q.getOrElse("")),
                set = q => update(_.copy(q = q)),
                placeholderText = "Filter features…"
              )
            )
          ),
          child <-- pageSignal.map(p =>
            board(Search.filter(entries, p.q.getOrElse("")), versionOf(p), released)
          )
        )
      }
    )

  private def versionLabel(v: VersionId, versions: List[Version]): String =
    versions.find(_.version == v) match
      case Some(reg) =>
        val lts        = if reg.lts then " LTS" else ""
        val unreleased = if reg.released then "" else " (unreleased)"
        s"${v.render}$lts$unreleased"
      case None => v.render

  /** Newest version first, so within Stable the recently landed features lead. */
  private val cardOrder: Ordering[(EntrySummary, BoardCell)] =
    Ordering
      .by[(EntrySummary, BoardCell), Option[VersionId]](_._2.status.flatMap(_.version))
      .reverse
      .orElseBy(_._1.title)

  private def sections(
      cells: List[(EntrySummary, BoardCell)],
      columns: List[BoardColumn],
      upcoming: Boolean
  ): List[Components.BoardSection] =
    val byColumn = cells.groupBy(_._2.column)
    columns.map { c =>
      Components.BoardSection(
        label = BoardColumn.label(c),
        colorCls = Components.columnClasses(c),
        cards = byColumn.getOrElse(c, Nil).sorted(using cardOrder).map(card(_, upcoming))
      )
    }

  /** The board for `v`, then either the upcoming section or — on a version older than the latest
    * release — a note pointing at where upcoming work is shown. A historical board is a snapshot of
    * that version, so work that had not landed by then is deliberately absent from it.
    */
  private def board(
      entries: List[EntrySummary],
      v: VersionId,
      released: Set[VersionId]
  ): HtmlElement =
    val latest     = released.maxOption
    val historical = latest.exists(v < _)
    val inVersion  = entries.flatMap(e => Board.cell(e.availability, e.archived, v).map(e -> _))
    val ahead      = entries.flatMap(e => Board.upcoming(e.availability, e.archived, v).map(e -> _))
    div(
      Components.board(sections(inVersion, BoardColumn.inVersion, upcoming = false)),
      if historical then
        div(
          cls := "mt-10 rounded-lg border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-600",
          s"Showing Scala ${v.render} as it was. ",
          latest
            .map(l => span(s"To see upcoming work, select ${l.render} or newer."))
            .getOrElse(emptyNode)
        )
      else if ahead.isEmpty then emptyNode
      else
        div(
          cls := "mt-10 pt-8 border-t border-slate-200",
          Components.pageTitle("Upcoming"),
          Components.subtitle(s"Not in ${v.render} yet."),
          div(
            cls := "mt-5",
            Components.board(sections(ahead, BoardColumn.upcoming, upcoming = true))
          )
        )
    )

  /** The availability line, dropped for an idea (no availability) or an open pull request — there
    * is no version to name in either case.
    */
  private def card(item: (EntrySummary, BoardCell), upcoming: Boolean): HtmlElement =
    val (e, cell) = item
    val availability = cell.status
      .filter(_.stage != AvailabilityStage.PullRequest)
      .map(Components.statusText(_, upcoming))
    Components.boardCard(
      slug = e.slug,
      cardTitle = e.title,
      tooltip = e.tagline,
      sip = e.sip,
      availability = availability
    )
