package com.jpro.routing.sessionmanager

import java.net.URL
import java.net.URLDecoder
import com.jpro.routing.{Response, View, WebApp}
import com.jpro.webapi.{InstanceCloseListener, ScriptResultListener, WebAPI, WebCallback}
import de.sandec.jmemorybuddy.JMemoryBuddyLive
import javafx.beans.property.{ObjectProperty, Property, SimpleStringProperty, StringProperty}
import javafx.collections.{FXCollections, ObservableList}
import simplefx.all._
import simplefx.core._
import simplefx.experimental._
import java.net.URI


trait SessionManager { THIS =>

  def webApp: WebApp

  var ganalytics = false
  var gtags = false
  var trackingID = ""

  val getHistoryBackward: ObservableList[String] = FXCollections.observableArrayList()
  val currentHistoryProperty: StringProperty = new SimpleStringProperty("")
  val getHistoryForwards: ObservableList[String] = FXCollections.observableArrayList()

  @Bind var historyBackward: List[String] = getHistoryBackward.toBindable
  @Bind var historyCurrent : String = currentHistoryProperty.toBindable
  @Bind var historyForward : List[String] = getHistoryForwards.toBindable

  @Bind var url: String = null
  @Bind var view: View = null

  def goBack(): Unit
  def goForward(): Unit
  def gotoURL(url: String): Unit = gotoURL(url,true,true)
  def gotoURL(url: String, pushState: Boolean = true, track: Boolean = true): Unit = {
    val url2 = SessionManager.mergeURLs(THIS.url, url)
    println(s"goto: $url")
    val newView = if(view != null && view.handleURL(url)) FXFuture(view) else {
      getView(url2)
    }
    newView.map { view =>
      gotoURL(url2, view, pushState, track)
    }
  }

  def gotoURL(_url: String, x: Response, pushState: Boolean, track: Boolean): Unit

  def getView(url: String): FXFuture[Response] = {
    val node = if(view == null) null else view.realContent
    webApp.route(url, node)
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

  def mergeURLs(orig: String, next: String): String = {
    if(orig == null) next
    else new URI(orig).resolve(next).toString
  }
}
