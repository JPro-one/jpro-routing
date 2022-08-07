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
import java.util.function.Consumer
import java.awt.Desktop


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
  def isExternal(x: String): Boolean = x.startsWith("http")
  def gotoURL(url: String): Unit = {
    if(isExternal(url) && !WebAPI.isBrowser()) {
      SessionManager.externalLinkImpl.accept(url)
    } else {
      gotoURL(url,true,true)
    }
  }
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
  def markViewCollectable(oldView: View, newView: View): Unit = {
    //println("depths: " + viewDepth(oldView) + " - " + viewDepth(newView))
    if(oldView.realContent != newView.realContent) {
      //println("nodes: " + oldView.realContent + " - " + newView.realContent)
      JMemoryBuddyLive.markCollectable(s"Page url: ${oldView.url} title: ${oldView.title}", oldView.realContent)
    }
    if(oldView.subView() != null && newView.subView != null) {
      markViewCollectable(oldView.subView(), newView.subView())
    }
  }
  def viewDepth(x: View): Int = {
    if(x.subView() == null) 1 else 1 + viewDepth(x.subView())
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

  def setExternalLinkImpl(f: Consumer[String]) = externalLinkImpl = f
  var externalLinkImpl: Consumer[String] = { url =>
    // Opens link in the default browser
    if (Desktop.isDesktopSupported && !WebAPI.isBrowser) {
      Desktop.getDesktop.browse(URI.create(url))
    } else {
      println("Desktop is not supported")
    }
  }
}
