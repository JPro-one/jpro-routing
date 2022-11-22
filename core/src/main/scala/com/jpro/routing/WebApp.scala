package com.jpro.routing

import com.jpro.routing.sessionmanager.SessionManager
import com.jpro.webapi.WebAPI
import simplefx.core._
import simplefx.all._
import simplefx.experimental._

class WebApp(stage: Stage) extends StackPane { THIS =>

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

  def setRoute(x: Route): Unit = newRoute = x
  var newRoute: Route = RouteUtils.EmptyRoute

  def route(s: String, oldView: Node) = {
    newRoute(Request.fromString(s).copy(oldContent = oldView, origOldContent = oldView))
  }
  def route = {
    (s: String) => newRoute(Request.fromString(s))
  }
  def addRouteScalaFuture(fun: PartialFunction[String, FXFuture[Response]]): Unit = {
    val oldRoute = newRoute
    newRoute = (r) => {
      val res = oldRoute(r)
      if(res != null) res
      else fun.lift(r.path).getOrElse(null)
    }
  }
  def addRouteScala(fun: PartialFunction[String, Response]): Unit = {
    val oldRoute = newRoute
    newRoute = (r) => {
      val res = oldRoute(r)
      if(res != null) res
      else fun.lift(r.path).map(x => FXFuture(x)).getOrElse(null)
    }
  }
  def addRoute(fun: java.util.function.Function[String,Response]) = {
    val oldRoute = newRoute
    newRoute = (r) => {
      val res = oldRoute(r)
      if(res != null) res
      else {
        val res2 = fun(r.path)
        if(res2 == null) null
        else FXFuture(res2)
      }
    }
  }


  def start(sessionManager: SessionManager) = {
    SessionManagerContext.setContext(this, sessionManager)
    sessionManager.start()
  }

}
