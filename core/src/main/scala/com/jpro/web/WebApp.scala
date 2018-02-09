package com.jpro.web

import com.jpro.webapi.WebAPI
import simplefx.core._
import simplefx.all._

class WebApp(stage: Stage) extends StackPane { THIS =>

  var webAPI = if(WebAPI.isBrowser) com.jpro.webapi.WebAPI.getWebAPI(stage) else null

  val sessionManager = new SessionManager {
    override def webAPI = THIS.webAPI
    override def getView(url: String): Result = {
      println("getting: " + url)
      val view = route(url)
      view
    }
  }

  override def requestLayout(): Unit = {
    //println("request layout called!")
    if ((this.scene ne null) && WebAPI.isBrowser) {
      webAPI.requestLayout(this.scene)
    }
    super.requestLayout
  }

  SessionManagerContext.setContext(this, sessionManager)

  var route: PartialFunction[String, Result] = PartialFunction.empty
  def addRoute(fun: PartialFunction[String, Result]): Unit = {
    route = route orElse fun
  }

  children <-- List(sessionManager.page)

  def start() = {
    sessionManager.start()
  }

}
