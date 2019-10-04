package com.jpro.web.sessionmanager

import java.net.URL
import java.net.URLDecoder

import com.jpro.web.{Redirect, Result, View, WebApp}
import com.jpro.webapi.{InstanceCloseListener, ScriptResultListener, WebAPI, WebCallback}
import simplefx.all._
import simplefx.core._
import simplefx.experimental._


class SessionManagerDesktop(val webApp: WebApp) extends SessionManager { THIS =>

  override def getView(url: String): FXFuture[Result] = {
    println("getting: " + url)
    val view = webApp.route(url)
    view
  }

  def goBack(): Unit = {
    historyForward = historyCurrent :: historyForward
    historyCurrent = historyBackward.head
    historyBackward = historyBackward.tail
    goto(historyCurrent, false, true)
  }

  def goForward(): Unit = {
    assert(!historyForward.isEmpty, "Can't go forward, there is no entry in the forward history!")
    historyBackward = historyCurrent :: historyBackward
    historyCurrent = historyForward.head
    historyForward = historyForward.tail
    goto(historyCurrent, false, true)
  }

  def goto(_url: String, x: Result, pushState: Boolean, track: Boolean): Unit = {
    val url = URLDecoder.decode(_url,"UTF-8")
    x match {
      case Redirect(url) => goto(url)
      case view: View =>
        view.sessionManager = this
        view.url = url

        //setView() ???
        webApp.getTransition((THIS.view,view,!pushState)).doTransition(webApp,THIS.view,view)
        THIS.view = view

        if(pushState ) {
          historyForward = Nil
          if(historyCurrent != null) {
            historyBackward = historyCurrent :: historyBackward
          }
          historyCurrent = url
        }
    }
  }

  def start() = {

    goto("/", pushState = true)

  }
}
