package com.jpro.web

import java.net.URL
import java.net.URLDecoder

import com.jpro.webapi.{InstanceCloseListener, ScriptResultListener, WebAPI, WebCallback}
import simplefx.all._
import simplefx.core._

trait SessionManager { THIS =>

  var ganalytics = false

  def webAPI: WebAPI
  def goto(url: String): Unit = {
    println(s"goto: $url")
    val url2 = URLDecoder.decode(url,"UTF-8")
    val view = getView(url2)
    goto(url2, view, true)
  }
  def goto(_url: String, x: Result, pushState: Boolean): Unit = {
    val url = URLDecoder.decode(_url,"UTF-8")
    x match {
      case Redirect(url) => goto(url)
      case view: View =>
        view.url = url
        if(WebAPI.isBrowser) view.isMobile = webAPI.isMobile
        page = view.realContent

        if(WebAPI.isBrowser && webAPI != null) {
          if(pushState) {
            //webAPI.executeScript(s"""var doc = document.documentElement;
            //                        |history.replaceState({
            //                        |marker: "goto",
            //                        |scrollTop: (window.pageYOffset || doc.scrollTop)  - (doc.clientTop || 0)
            //                        |}, null, null);
            //                        |""".stripMargin)
            webAPI.executeScript(s"""history.pushState(null, null, "${view.url.replace("\"","\\\"")}");""")
          }
          val initialState = if(view.saveScrollPosition) "{saveScroll: true}" else "{saveScroll: false}"

          webAPI.executeScript(
          """var scrollY = 0;
            |if(history.state != null) {
            |  scrollY = history.state.scrollTop || 0;
            |}
            |scroll(0,scrollY)
            """.stripMargin)
          webAPI.executeScript(s"""document.getElementsByTagName("jpro-app")[0].sfxelem.setScrolling(${view.nativeScrolling})""")
          webAPI.executeScript("document.title = \"" + view.title + "\";")
          webAPI.executeScript(s"history.replaceState($initialState, null, null)")
          if(ganalytics) {
            webAPI.executeScript(s"""
            ga('set', {
              page: '${view.url}',
              title: '${view.title}'
            });

            // send it for tracking
            ga('send', 'pageview');
            """)
          }
        }
    }
  }
  def getView(url: String): Result
  def gotoURL(x: String, pushState: Boolean = true) = {
    val url = new URL(x)
    val newResult = getView(URLDecoder.decode(url.getFile(),"UTF-8"))
    goto(url.getFile(), newResult, false)
  }

  def start() = {
    if(webAPI != null) {
      gotoURL(webAPI.getServerName)
      println("registering popstate")
      webAPI.registerJavaFunction("popstatejava", new WebCallback {
        override def callback(s: String): Unit = {
          gotoURL(s.drop(1).dropRight(1).replace("\\\"","\""), true)
        }
      })
      webAPI.registerJavaFunction("jproGotoURL", new WebCallback {
        override def callback(s: String): Unit = {
          goto(s.drop(1).dropRight(1).replace("\\\"","\""))
        }
      })

      // Safari scrollsUp on popstate, when going back form external page (when scrollRestoration is manual)
      // when this happens, the ws-connection get's canceled by safari, which tells us,
      // that we have to move back to the saved scrollPosition.
      // we have to check, whether the ws is still alive, shortly after popstate.
      // we have to save the old scrollY immediately, so we remember it faster, than the safari resets it.
      webAPI.executeScript(
      s"""window.addEventListener("scroll", function(e) {
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
        |  window.setTimeout(function(){console.log("popstate called!")},3000);
        |  var scrollY = 0;
        |  if(history.state != null) {
        |    scrollY = history.state.scrollTop || 0;
        |  }
        |  window.setTimeout(function(){
        |    if(!document.getElementsByTagName("jpro-app")[0].jproimpl.isConnected) {
        |      window.setTimeout(function(){console.log("resetting scroly (wasn't connected")},3000);
        |      console.log("scrollY: " + scrollY);
        |      scroll(0,scrollY);
        |    }
        |  }, 1);
        |  jpro.popstatejava(location.href);
        |});""".stripMargin)
      webAPI.executeScript(
        """if ('scrollRestoration' in history) {
          |  // Back off, browser, I got this...
          |  history.scrollRestoration = 'manual';
          |}
        """.stripMargin)
    } else {
      goto("/")
    }
  }

  @Bind var url: String = null
  @Bind var page: Node = null
}
