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
        val oldView = this.view
        this.view = view
        this.url = _url
        view.sessionManager = this
        view.url = url

        isFullscreen = view.fullscreen
        webApp.getTransition((THIS.view,view,!pushState)).doTransition(container,oldView,view)
        if(THIS.view != null && THIS.view != view) {
          THIS.view.onClose()
          THIS.view.sessionManager = null
          markViewCollectable(THIS.view)
        }
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
  val container = new StackPane()
  val scrollpane = new ScrollPane() {
    fitToWidth = true
    content <-- container
    fitToHeight <-- isFullscreen
    vbarPolicy <-- (if(isFullscreen) ScrollPane.ScrollBarPolicy.NEVER else ScrollPane.ScrollBarPolicy.ALWAYS)
  }
  webApp <++ scrollpane
  @Bind var isFullscreen = true

  def start() = {
    goto("/", pushState = true)
  }
}
