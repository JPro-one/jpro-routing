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
      webAPI.requestLayout(this.scene)
      super.layoutChildren()
    } else {
      super.layoutChildren()
    }
  }

  lazy val webAPI = if(WebAPI.isBrowser) com.jpro.webapi.WebAPI.getWebAPI(stage) else null

  def setRoute(x: Route) = newRoute = x
  var newRoute: Route = (r) => null

  def route = {
    Function.unlift((s: String) => Option(newRoute(Request.fromString(s))))
  }
  def addRouteFuture(fun: PartialFunction[String, FXFuture[Response]]): Unit = {
    val oldRoute = newRoute
    newRoute = (r) => {
      val res = oldRoute(r)
      if(res != null) res
      else fun.lift(r.path).getOrElse(null)
    }
  }
  def addRoute(fun: PartialFunction[String, Response]): Unit = {
    val oldRoute = newRoute
    newRoute = (r) => {
      val res = oldRoute(r)
      if(res != null) res
      else fun.lift(r.path).map(x => FXFuture(x)).getOrElse(null)
    }
  }
  def addRouteJava(fun: java.util.function.Function[String,Response]) = {
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


  var transitionRoute: PartialFunction[(View,View,Boolean), PageTransition] = PartialFunction.empty
  var defaultTransition: PageTransition = PageTransition.InstantTransition
  def addTransition(fun: PartialFunction[(View,View,Boolean), PageTransition]): Unit = {
    transitionRoute = transitionRoute orElse fun
  }
  def getTransition(x: (View,View,Boolean)): PageTransition = transitionRoute.lift(x).getOrElse(defaultTransition)

  //def changeContent(parent: StackPane, oldNode: Node, newContent: Node)
  //children <-- List(sessionManager.page)

  def start(sessionManager: SessionManager) = {
    SessionManagerContext.setContext(this, sessionManager)
    sessionManager.start()
  }

}
