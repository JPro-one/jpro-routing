package com.jpro.routing.sessionmanager

import java.net.URL
import java.net.URLDecoder

import com.jpro.routing.{Redirect, Response, View, RouteNode}
import com.jpro.webapi.{InstanceCloseListener, ScriptResultListener, WebAPI, WebCallback}
import simplefx.all._
import simplefx.core._
import simplefx.experimental._


class SessionManagerWeb(val webApp: RouteNode, webAPI: WebAPI) extends SessionManager { THIS =>


  def goBack(): Unit = {
    webAPI.executeScript("history.go(-1);")

  }

  def goForward(): Unit = {
    webAPI.executeScript("history.go(1);")

  }

  webAPI.addInstanceCloseListener(() => {
    THIS.view.onClose()
    THIS.view.sessionManager = null
    markViewCollectable(THIS.view)
  })

  def gotoURL(_url: String, x: Response, pushState: Boolean, track: Boolean): Unit = {
    val url = URLDecoder.decode(_url,"UTF-8")
    x match {
      case Redirect(url) => gotoURL(url)
      case view: View =>
        this.url = url
        view.sessionManager = this
        view.url = url

        view.isMobile = webAPI.isMobile

        webApp.children = List(view.realContent)
        if(THIS.view != null && THIS.view != view) {
          THIS.view.onClose()
          THIS.view.sessionManager = null
          markViewCollectable(THIS.view, view)
        }
        THIS.view = view


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
        webAPI.executeScript(s"""document.getElementsByTagName("jpro-app")[0].sfxelem.setFXHeight(${!view.fullscreen})""")
        webAPI.executeScript(s"""document.title = "${view.title.replace("\"","\\\"")}";""")
        webAPI.executeScript(s"""document.querySelector('meta[name="description"]').setAttribute("content", "${view.description.replace("\"","\\\"")}");""")
        webAPI.executeScript(s"history.replaceState($initialState, null, null)")
        if(ganalytics && track) {
          webAPI.executeScript(s"""
                                  |ga('set', {
                                  |  page: "${view.url.replace("\"","\\\"")}",
                                  |  title: "${view.title.replace("\"","\\\"")}"
                                  |});
                                  |
          |// send it for tracking
                                  |ga('send', 'pageview');
          """.stripMargin)
        }
        if(gtags && track) {
          assert(!trackingID.isEmpty)
          webAPI.executeScript(s"""
                                  |gtag('config', '$trackingID', {
                                  |  'page_title' : "${view.title.replace("\"","\\\"")}",
                                  |  'page_location': "${view.title.replace("\"","\\\"")}"
                                  |});""".stripMargin)
        }

    }
  }

  def gotoFullEncodedURL(x: String, pushState: Boolean = true, track: Boolean = true): Unit = {
    gotoURL(URLDecoder.decode(x,"UTF-8"), pushState, track)
  }

  def start() = {
    gotoFullEncodedURL(webAPI.getServerName, false, false)
    println("registering popstate")
    webAPI.registerJavaFunction("popstatejava", new WebCallback {
      override def callback(s: String): Unit = {
        gotoFullEncodedURL(s.drop(1).dropRight(1).replace("\\\"","\""), false)
      }
    })
    webAPI.registerJavaFunction("jproGotoURL", new WebCallback {
      override def callback(s: String): Unit = {
        gotoURL(s.drop(1).dropRight(1).replace("\\\"","\""))
      }
    })

    webAPI.executeScript(
      s"""var scheduled = false
         |window.addEventListener("scroll", function(e) {
         |  if(!scheduled) {
         |    window.setTimeout(function(){
         |      scheduled = false;
         |      var doc = document.documentElement;
         |      if(history.state != null && history.state.saveScroll) {
         |        history.replaceState({
         |          marker: "pop",
         |          saveScroll: true,
         |          scrollTop: (window.pageYOffset || doc.scrollTop)  - (doc.clientTop || 0)
         |        }, null, null);
         |      }
         |    },300);
         |  }
         |  scheduled = true;
         |});
         |""".stripMargin)
    // Safari scrollsUp on popstate, when going back form external page (when scrollRestoration is manual)
    // when this happens, the ws-connection get's canceled by safari, which tells us,
    // that we have to move back to the saved scrollPosition.
    // we have to check, whether the ws is still alive, shortly after popstate.
    // we have to save the old scrollY immediately, so we remember it faster, than the safari resets it.
    webAPI.executeScript("""
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
      // Back off, browser, I got this...
      """if ('scrollRestoration' in history) {
        |  history.scrollRestoration = 'manual';
        |}
      """.stripMargin)

  }
}
