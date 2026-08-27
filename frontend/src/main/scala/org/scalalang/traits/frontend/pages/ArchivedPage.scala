package org.scalalang.traits.frontend.pages

import org.scalalang.traits.frontend.ui.{Components, Loaded}
import org.scalalang.traits.frontend.Api
import org.scalalang.traits.frontend.Api.given
import org.scalalang.traits.shared.*
import com.raquo.laminar.api.L.*

/** Archived entries — the only place they show, since every board leaves them out. One flat grid of
  * the same cards, each saying how far the change got before it was set aside.
  */
object ArchivedPage:

  def apply(): HtmlElement =
    val state = Var[Loaded[(List[EntrySummary], List[Version])]](Loaded.Loading)

    Api.listEntries().zip(Api.listVersions()).foreach {
      case (Right(es), Right(vs)) => state.set(Loaded.Ok((es, vs)))
      case (Left(e), _)           => state.set(Loaded.Failed(e.message))
      case (_, Left(e))           => state.set(Loaded.Failed(e.message))
    }

    Components.containerWide(
      div(cls := "mb-5", Components.pageTitle("Archived")),
      Components.loaded(state.signal) { case (entries, versions) =>
        val latest   = Version.latestReleased(versions)
        val archived = entries.filter(_.archived).sortBy(_.title)
        if archived.isEmpty then div(cls := "text-sm text-slate-500", "Nothing is archived.")
        else
          Components.cardGrid(
            archived.map(e =>
              Components.boardCard(
                slug = e.slug,
                cardTitle = e.title,
                tooltip = e.tagline,
                sip = e.sip,
                availability = Components.reachedText(e.availability, latest)
              )
            )
          )
      }
    )
