package com.jpro.web

import java.net.URL

import com.jpro.webapi.{InstanceCloseListener, ScriptResultListener, WebAPI, WebCallback}
import simplefx.all._
import simplefx.core._

trait SessionManager { THIS =>

  def webAPI: WebAPI
  def goto(url: String): Unit = {
    println(s"goto: $url")
    val view = getView(url)
    goto(url, view, true)
  }
  def goto(url: String, x: Result, pushState: Boolean): Unit = {
    x match {
      case Redirect(url) => goto(url)
      case view: View =>
        view.url = url
        page = view.realContent

        if(WebAPI.isBrowser && webAPI != null) {
          if(pushState) {
            //webAPI.executeScript(s"""var doc = document.documentElement;
            //                        |history.replaceState({
            //                        |marker: "goto",
            //                        |scrollTop: (window.pageYOffset || doc.scrollTop)  - (doc.clientTop || 0)
            //                        |}, null, null);
            //                        |""".stripMargin)
            webAPI.executeScript(s"history.pushState(null, null, '${view.url}');")
          }
          val initialState = if(view.saveScrollPosition) "{saveScroll: true}" else "{saveScroll: false}"

          webAPI.executeScript(
          """var scrollY = 0;
              |if(history.state != null) {
              |  scrollY = history.state.scrollTop || 0;
              |}
              |scroll(0,scrollY)
            """.stripMargin)
          webAPI.executeScript("document.title = \"" + view.title + "\";")
          webAPI.executeScript(s"history.replaceState($initialState, null, null)")
        }
    }
  }
  def getView(url: String): Result
  def gotoURL(x: String, pushState: Boolean = true) = {
    val url = new URL(x)
    val newResult = getView(url.getFile())
    goto(url.getFile(), newResult, false)
  }

  def start() = {
    if(webAPI != null) {
      gotoURL(webAPI.getServerName)
      println("registering popstate")
      webAPI.registerJavaFunction("popstatejava", new WebCallback {
        override def callback(s: String): Unit = {
          gotoURL(s.drop(1).dropRight(1), true)
        }
      })
      webAPI.executeScript(
      """window.addEventListener("scroll", function(e) {
        |  console.log("got e");
        |  console.log("e" + location.href);
        |  var doc = document.documentElement;
        |  if(history.state != null && history.state.saveScroll) {
        |    history.replaceState({
        |      marker: "pop",
        |      saveScroll: true,
        |      scrollTop: (window.pageYOffset || doc.scrollTop)  - (doc.clientTop || 0)
        |    }, null, null);
        |  }
        |
        |});
        |window.addEventListener('popstate', function(e) {
        |  jpro.popstatejava(location.href);
        |});""".stripMargin)
    } else {
      goto("/")
    }
  }

  @Bind var url: String = null
  @Bind var page: Node = null
}
