package com.jpro.routing

import com.jpro.routing.sessionmanager.SessionManager
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

abstract class RouteApp extends Application {

  override def start(stage: Stage): Unit = {
    val routeNode = new RouteNode(stage)
    routeNode.setRoute(getRoute())
    val scene = new Scene(routeNode, 1400, 800)
    stage.setScene(scene)
    stage.show()
    routeNode.start(SessionManager.getDefault(routeNode, stage))
  }

  def getRoute(): Route
}
