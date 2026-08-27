package org.scalalang.traits.frontend

import org.scalalang.traits.frontend.pages.{
  ArchivedPage,
  BoardPage,
  EditorPage,
  EntryPage,
  LoginPage,
  SipBoardPage,
  VersionsPage
}
import org.scalalang.traits.frontend.Api.given
import com.raquo.laminar.api.L.*
import com.raquo.waypoint.SplitRender
import org.scalajs.dom

object Main:

  // Whether anything is archived at all, so the tab only shows when it leads somewhere. Fetched
  // once at load; an editor who archives an entry sees the tab appear on the next reload.
  private val hasArchived = Var(false)

  def main(args: Array[String]): Unit =
    Session.init()
    Api.listEntries().foreach {
      case Right(es) => hasArchived.set(es.exists(_.archived))
      case Left(_)   => ()
    }
    val _ = renderOnDomContentLoaded(dom.document.getElementById("app"), app())

  def app(): HtmlElement =
    div(
      cls := "min-h-screen bg-slate-50 text-slate-900",
      navbar(),
      child <-- pageViews
    )

  // SplitRender (not a plain map) so that changes within one page value — a board's URL query
  // params — update the mounted page in place instead of rebuilding it, which would refetch and
  // drop input focus on every keystroke.
  private lazy val pageViews: Signal[HtmlElement] =
    SplitRender[Page, HtmlElement](Routes.router.currentPageSignal)
      .collectSignal[Page.Home](BoardPage(_))
      .collectSignal[Page.Sips](SipBoardPage(_))
      .collectStatic(Page.Versions)(VersionsPage())
      .collectStatic(Page.Archived)(ArchivedPage())
      .collectStatic(Page.Login)(LoginPage())
      .collectStatic(Page.NewEntry)(EditorPage(None))
      .collect[Page.EntryView](p => EntryPage(p.slug))
      .collect[Page.EditEntry](p => EditorPage(Some(p.slug)))
      .signal

  private def navbar(): HtmlElement =
    div(
      cls := "bg-white border-b border-slate-200",
      div(
        cls := "max-w-7xl mx-auto px-4 h-14 flex items-center gap-6",
        navLink(Page.Home(), "Traits", "font-semibold text-slate-900 hover:text-slate-900"),
        div(
          cls := "flex gap-4 text-sm",
          navLink(Page.Home(), "Pipeline"),
          navLink(Page.Sips(), "SIPs"),
          child <-- hasArchived.signal.map {
            case true  => navLink(Page.Archived, "Archived")
            case false => emptyNode
          },
          navLink(Page.Versions, "Versions")
        ),
        div(
          cls := "ml-auto flex items-center gap-4 text-sm",
          child <-- Session.current.signal.map(editorControls)
        )
      )
    )

  private def editorControls(editor: Option[org.scalalang.traits.shared.Editor]): HtmlElement =
    editor match
      case Some(_) =>
        div(
          cls := "flex items-center gap-3",
          navLink(Page.NewEntry, "+ New entry", "text-blue-600 hover:underline font-medium"),
          button(
            cls := "text-slate-500 hover:text-slate-900",
            "Sign out",
            onClick --> { _ =>
              Api.logout().foreach { _ =>
                Session.clear()
                Routes.router.pushState(Page.Home())
              }
            }
          )
        )
      case None =>
        navLink(Page.Login, "Sign in", "text-slate-600 hover:text-slate-900")

  private def navLink(
      page: Page,
      text: String,
      extraCls: String = "text-slate-600 hover:text-slate-900"
  ): HtmlElement =
    a(
      cls  := extraCls,
      href := Routes.urlFor(page),
      onClick.preventDefault --> { _ => Routes.router.pushState(page) },
      text
    )
