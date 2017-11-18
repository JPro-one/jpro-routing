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
          webAPI.executeScript(s"history.pushState(null, null, '${view.url}');")
          webAPI.executeScript("document.title = \"" + view.title + "\";")
        }
    }
  }
  def getView(url: String): Result
  def gotoURL(x: String) = {
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
          gotoURL(s.drop(1).dropRight(1))
        }
      })
      webAPI.executeScript("""window.addEventListener('popstate', function(e) {
          console.log("got e");
          console.log("e" + location.href);
          jpro.popstatejava(location.href);
        });""")
    } else {
      goto("/")
    }
  }

  @Bind var url: String = null
  @Bind var page: Node = null
}
