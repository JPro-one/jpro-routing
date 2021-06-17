package com.jpro.web.sessionmanager

import java.net.URL
import java.net.URLDecoder

import com.jpro.web.{Result, View, WebApp}
import com.jpro.webapi.{InstanceCloseListener, ScriptResultListener, WebAPI, WebCallback}
import de.sandec.jmemorybuddy.JMemoryBuddyLive
import simplefx.all._
import simplefx.core._
import simplefx.experimental._


trait SessionManager {

  def webApp: WebApp

  var ganalytics = false
  var gtags = false
  var trackingID = ""

  @Bind var historyBackward: List[String] = Nil
  @Bind var historyCurrent : String = null
  @Bind var historyForward : List[String] = Nil

  @Bind var url: String = null
  @Bind var view: View = null

  def goBack(): Unit
  def goForward(): Unit
  def goto(url: String, pushState: Boolean = true, track: Boolean = true): Unit = {
    println(s"goto: $url")
    val url2 = URLDecoder.decode(url,"UTF-8")
    val newView = if(view != null && view.handleURL(url2)) FXFuture(view) else {
      getView(url2)
    }
    newView.map { view =>
      goto(url2, view, pushState, track)
    }
  }


  def goto(_url: String, x: Result, pushState: Boolean, track: Boolean): Unit

  def getView(url: String): FXFuture[Result]
  def gotoURL(x: String, pushState: Boolean = true, track: Boolean = true) = {
    val url = new URL(x)
    val newView = if(view != null && view.handleURL(url.getFile())) FXFuture(view) else {
      println("Keeping view")
      getView(URLDecoder.decode(url.getFile(),"UTF-8"))
    }
    newView.map { newResult =>
      goto(url.getFile(), newResult, pushState, track)
    }
  }
  def start(): Unit

  def markViewCollectable(view: View): Unit = {
    JMemoryBuddyLive.markCollectable(s"Page url: ${view.url} title: ${view.title}", view.realContent)
  }
}

object SessionManager {
  def getDefault(app: WebApp, stage: Stage): SessionManager = {
    if(WebAPI.isBrowser) new SessionManagerWeb(app, WebAPI.getWebAPI(stage))
    else new SessionManagerDesktop(app)
  }
}
