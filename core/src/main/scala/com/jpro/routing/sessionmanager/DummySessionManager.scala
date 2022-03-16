package com.jpro.routing.sessionmanager
import com.jpro.routing.{Result, WebApp}

class DummySessionManager extends SessionManager {
  override def webApp: WebApp = null

  override def goBack(): Unit = ()

  override def goForward(): Unit = ()

  override def gotoURL(_url: String, x: Result, pushState: Boolean, track: Boolean): Unit = ()

  override def getView(url: String): _root_.simplefx.experimental.FXFuture[Result] = null

  override def start(): Unit = ()
}
