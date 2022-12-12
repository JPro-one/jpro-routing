package com.jpro.routing

import com.jpro.routing.sessionmanager.SessionManager
import com.jpro.webapi.WebAPI
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.Parent

abstract class RouteApp extends Application {

  private var _stage: Stage = null;
  private var routeNode: RouteNode = null
  private var _scene: Scene = null
  def getStage(): Stage = _stage
  def getScene(): Scene = _scene
  def getRouteNode(): RouteNode = routeNode
  def getWebAPI(): WebAPI = if(WebAPI.isBrowser) WebAPI.getWebAPI(getStage()) else null

  override def start(stage: Stage): Unit = {
    _stage = stage
    routeNode = new RouteNode(stage)
    _scene = new Scene(routeNode, 1400, 800)
    stage.setScene(_scene)
    routeNode.setRoute(createRoute())
    stage.show()
    routeNode.start(SessionManager.getDefault(routeNode, stage))
  }

  def createRoute(): Route
}
