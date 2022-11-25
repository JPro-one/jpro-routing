package com.jpro.routing

import com.jpro.routing.sessionmanager.SessionManager
import com.jpro.webapi.WebAPI
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

abstract class RouteApp extends Application {

  private var _stage = null;
  def getStage(): Stage = _stage

  def getWebAPI(): WebAPI = WebAPI.getWebAPI(getStage())

  override def start(stage: Stage): Unit = {
    _stage = null
    val routeNode = new RouteNode(stage)
    routeNode.setRoute(getRoute())
    val scene = new Scene(routeNode, 1400, 800)
    stage.setScene(scene)
    stage.show()
    routeNode.start(SessionManager.getDefault(routeNode, stage))
  }

  def getRoute(): Route
}
