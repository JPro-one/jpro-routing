package com.jpro.web

import com.jpro.web.sessionmanager.SessionManager
import com.jpro.webapi.WebAPI
import simplefx.core._
import simplefx.all._
import simplefx.experimental._

class WebApp(stage: Stage) extends StackPane { THIS =>

  styleClass ::= "jpro-web-app"

  override def requestLayout(): Unit = {
    if ((this.scene ne null) && WebAPI.isBrowser) {
      WebAPI.getWebAPI(stage).requestLayout(this.scene)
    }
    super.requestLayout
  }


  lazy val webAPI = if(WebAPI.isBrowser) com.jpro.webapi.WebAPI.getWebAPI(stage) else null


  var route: PartialFunction[String, FXFuture[Result]] = PartialFunction.empty
  def addRouteFuture(fun: PartialFunction[String, FXFuture[Result]]): Unit = {
    route = route orElse fun
  }
  def addRoute(fun: PartialFunction[String, Result]): Unit = {
    route = route orElse fun.andThen(x => FXFuture(x))
  }
  def addRouteJava(fun: java.util.function.Function[String,Result]) = {
    class Extractor[A, B](val f: A => Option[B]) {
      def unapply(a: A) = f(a)
    }

    def unlift[A, B](f: A => Option[B]): PartialFunction[A, B] = {
      val LocalExtractor = new Extractor(f)

      // Create the PartialFunction from a partial function literal
      { case LocalExtractor(b) => b }
    }

    addRoute(unlift((x: String) => Option(fun(x))))
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
