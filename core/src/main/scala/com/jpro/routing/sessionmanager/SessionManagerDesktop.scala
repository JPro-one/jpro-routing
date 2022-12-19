package com.jpro.routing.sessionmanager

import java.net.URL
import java.net.URLDecoder

import com.jpro.routing.{Redirect, Response, View, RouteNode}
import com.jpro.webapi.{InstanceCloseListener, ScriptResultListener, WebAPI, WebCallback}
import simplefx.all._
import simplefx.core._
import simplefx.experimental._


class SessionManagerDesktop(val webApp: RouteNode) extends SessionManager { THIS =>


  def goBack(): Unit = {
    historyForward = historyCurrent :: historyForward
    historyCurrent = historyBackward.head
    historyBackward = historyBackward.tail
    gotoURL(historyCurrent, false, true)
  }

  def goForward(): Unit = {
    assert(!historyForward.isEmpty, "Can't go forward, there is no entry in the forward history!")
    historyBackward = historyCurrent :: historyBackward
    historyCurrent = historyForward.head
    historyForward = historyForward.tail
    gotoURL(historyCurrent, false, true)
  }

  def gotoURL(_url: String, x: Response, pushState: Boolean, track: Boolean): Unit = {
    val url = URLDecoder.decode(_url,"UTF-8")
    x match {
      case Redirect(url) => 
        println(s"redirect: ${_url} -> $url")
        gotoURL(url)
      case view: View =>
        val oldView = this.view
        this.view = view
        this.url = _url
        view.sessionManager = this
        view.url = url

        isFullscreen = view.fullscreen
        container.children = List(view.realContent)
        if(oldView != null && oldView != view) {
          oldView.onClose()
          oldView.sessionManager = null
          markViewCollectable(oldView, view)
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
    //this.padding = Insets(0)
    style = "-fx-padding: 0;"
    background = Background.EMPTY
     vbarPolicy <-- (if(isFullscreen) ScrollPane.ScrollBarPolicy.NEVER else ScrollPane.ScrollBarPolicy.AS_NEEDED)
  }
  webApp <++ scrollpane
  @Bind var isFullscreen = true

  def start() = {
    gotoURL("/", pushState = true)
  }
}
