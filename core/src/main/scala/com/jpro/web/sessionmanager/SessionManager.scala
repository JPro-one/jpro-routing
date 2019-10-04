package com.jpro.web.sessionmanager

import java.net.URL
import java.net.URLDecoder

import com.jpro.web.{Result, View, WebApp}
import com.jpro.webapi.{InstanceCloseListener, ScriptResultListener, WebAPI, WebCallback}
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
    getView(url2).map { view =>
      goto(url2, view, pushState, track)
    }
  }


  def goto(_url: String, x: Result, pushState: Boolean, track: Boolean): Unit

  def getView(url: String): FXFuture[Result]
  def gotoURL(x: String, pushState: Boolean = true, track: Boolean = true) = {
    val url = new URL(x)
    getView(URLDecoder.decode(url.getFile(),"UTF-8")).map { newResult =>
      goto(url.getFile(), newResult, pushState, track)
    }
  }
  def start(): Unit

}

object SessionManager {
  def getDefault(app: WebApp, stage: Stage): SessionManager = {
    if(WebAPI.isBrowser) new SessionManagerWeb(app, WebAPI.getWebAPI(stage))
    else new SessionManagerDesktop(app)
  }
}
