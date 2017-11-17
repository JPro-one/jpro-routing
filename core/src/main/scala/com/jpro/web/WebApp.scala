package com.jpro.web

import com.jpro.webapi.WebAPI
import simplefx.core._
import simplefx.all._

class WebApp(stage: Stage) extends StackPane { THIS =>

  var webAPI = if(WebAPI.isBrowser) com.jpro.webapi.WebAPI.getWebAPI(stage) else null

  val sessionManager = new SessionManager {
    override def webAPI = THIS.webAPI
    override def getView(url: String): View = {
      println("getting: " + url)
      val view = route(url)
      println("title: " + view.title)
      view
    }
  }

  SessionManagerContext.setContext(this, sessionManager)

  var route: PartialFunction[String, View] = PartialFunction.empty[String, View]
  def addRoute(fun: PartialFunction[String, View]): Unit = {
    route = route orElse fun
  }

  children <-- List(sessionManager.page)

  def start() = {
    sessionManager.start()
  }

}
