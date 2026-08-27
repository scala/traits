package org.scalalang.traits.frontend.pages

import org.scalalang.traits.frontend.ui.{Components, Loaded}
import org.scalalang.traits.frontend.{Api, Page, Routes}
import org.scalalang.traits.frontend.Api.given
import org.scalalang.traits.shared.*
import com.raquo.laminar.api.L.*

/** The SIP board: same cards and layout as the pipeline, one stacked section per SIP stage, ending
  * with a Closed section for rejected and withdrawn proposals. Only entries with a SIP appear here.
  * The text filter is a URL query param (`/sips?q=match`), as on the pipeline board.
  */
object SipBoardPage:

  // `None` is the Closed section (SipState.stage is None for Rejected | Withdrawn).
  private val stages: List[Option[SipStage]] =
    SipStage.values.toList.map(Some(_)) :+ None

  private def stageLabel(c: Option[SipStage]): String = c match
    case Some(SipStage.PreSip)         => "Pre-SIP"
    case Some(SipStage.Design)         => "Design"
    case Some(SipStage.Implementation) => "Implementation"
    case Some(SipStage.Completed)      => "Completed"
    case None                          => "Closed"

  private def stageClasses(c: Option[SipStage]): String = c match
    case Some(SipStage.PreSip)         => "bg-slate-100 text-slate-600"
    case Some(SipStage.Design)         => "bg-amber-100 text-amber-700"
    case Some(SipStage.Implementation) => "bg-violet-100 text-violet-700"
    case Some(SipStage.Completed)      => "bg-emerald-100 text-emerald-700"
    case None                          => "bg-rose-100 text-rose-700"

  def apply(pageSignal: Signal[Page.Sips]): HtmlElement =
    val state = Var[Loaded[(List[EntrySummary], List[Version])]](Loaded.Loading)

    // Versions only to tell a shipped availability from one still ahead of the latest release.
    Api.listEntries().zip(Api.listVersions()).foreach {
      case (Right(es), Right(vs)) => state.set(Loaded.Ok((es, vs)))
      case (Left(e), _)           => state.set(Loaded.Failed(e.message))
      case (_, Left(e))           => state.set(Loaded.Failed(e.message))
    }

    def update(q: Option[String]): Unit =
      Routes.router.currentPageSignal.now() match
        case _: Page.Sips => Routes.router.replaceState(Page.Sips(q))
        case _            => ()

    Components.containerWide(
      div(
        cls := "flex flex-wrap items-end justify-between gap-3 mb-5",
        Components.pageTitle("Scala improvement proposals"),
        Components.filterInput(
          term = pageSignal.map(_.q.getOrElse("")),
          set = update,
          placeholderText = "Filter proposals…"
        )
      ),
      Components.loaded(state.signal) { case (entries, versions) =>
        val latest = Version.latestReleased(versions)
        div(child <-- pageSignal.map { p =>
          val withSip = Search
            .filter(entries.filterNot(_.archived), p.q.getOrElse(""))
            .flatMap(e => e.sip.map(e -> _))
          Components.board(
            stages.map { c =>
              val items = withSip
                .filter((_, sip) => SipState.stage(sip.state) == c)
                .sortBy(_._1.title)
              Components.BoardSection(
                label = stageLabel(c),
                colorCls = stageClasses(c),
                cards = items.map(card(_, latest))
              )
            }
          )
        })
      }
    )

  private def card(item: (EntrySummary, Sip), latest: Option[VersionId]): HtmlElement =
    val (e, sip) = item
    Components.boardCard(
      slug = e.slug,
      cardTitle = e.title,
      tooltip = e.tagline,
      sip = Some(sip),
      availability = Components.reachedText(e.availability, latest)
    )
