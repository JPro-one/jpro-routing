package com.jpro.routing

import com.jpro.routing.sessionmanager.SessionManager
import com.jpro.webapi.WebAPI
import simplefx.core._
import simplefx.all._
import simplefx.experimental._

import java.lang.ref.WeakReference

class RouteNode(stage: Stage) extends StackPane { THIS =>

  styleClass ::= "jpro-web-app"

  override def layoutChildren(): Unit = {
    if ((this.scene ne null) && WebAPI.isBrowser) {
      webAPI.layoutRoot(this.scene)
      super.layoutChildren()
    } else {
      super.layoutChildren()
    }
  }

  lazy val webAPI = if(WebAPI.isBrowser) com.jpro.webapi.WebAPI.getWebAPI(stage) else null

  def getRoute(): Route = newRoute
  def setRoute(x: Route): Unit = newRoute = x
  var newRoute: Route = Route.empty()

  def route(s: String, oldView: Node) = {
    val oldViewW = new WeakReference(oldView)
    newRoute(Request.fromString(s).copy(oldContent = oldViewW, origOldContent = oldViewW))
  }
  def route = {
    (s: String) => newRoute(Request.fromString(s))
  }


  def start(sessionManager: SessionManager) = {
    SessionManagerContext.setContext(this, sessionManager)
    sessionManager.start()
  }



}
