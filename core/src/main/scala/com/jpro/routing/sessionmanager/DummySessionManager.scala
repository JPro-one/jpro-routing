package com.jpro.routing.sessionmanager
import com.jpro.routing.{Response, WebApp}

class DummySessionManager extends SessionManager {
  override def webApp: WebApp = null

  override def goBack(): Unit = ()

  override def goForward(): Unit = ()

  override def gotoURL(_url: String, x: Response, pushState: Boolean, track: Boolean): Unit = ()

  override def getView(url: String): _root_.simplefx.experimental.FXFuture[Response] = null

  override def start(): Unit = ()
}
