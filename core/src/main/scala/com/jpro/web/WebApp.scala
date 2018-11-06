package com.jpro.web

import com.jpro.webapi.WebAPI
import simplefx.core._
import simplefx.all._
import simplefx.experimental._

class WebApp(stage: Stage) extends StackPane { THIS =>

  lazy val webAPI = if(WebAPI.isBrowser) com.jpro.webapi.WebAPI.getWebAPI(stage) else null

  lazy val sessionManager = new SessionManager {
    override def webApp = THIS
    override def webAPI = THIS.webAPI
    override def getView(url: String): FXFuture[Result] = {
      println("getting: " + url)
      val view = route(url)
      view
    }
  }


  SessionManagerContext.setContext(this, sessionManager)

  var route: PartialFunction[String, FXFuture[Result]] = PartialFunction.empty
  def addRouteFuture(fun: PartialFunction[String, FXFuture[Result]]): Unit = {
    route = route orElse fun
  }
  def addRoute(fun: PartialFunction[String, Result]): Unit = {
    route = route orElse fun.andThen(x => FXFuture(x))
  }

  var transitionRoute: PartialFunction[(View,View,Boolean), PageTransition] = PartialFunction.empty
  var defaultTransition: PageTransition = PageTransition.InstantTransition
  def addTransition(fun: PartialFunction[(View,View,Boolean), PageTransition]): Unit = {
    transitionRoute = transitionRoute orElse fun
  }
  def getTransition(x: (View,View,Boolean)): PageTransition = transitionRoute.lift(x).getOrElse(defaultTransition)

  //def changeContent(parent: StackPane, oldNode: Node, newContent: Node)
  //children <-- List(sessionManager.page)

  def start() = {
    sessionManager.start()
  }

}
