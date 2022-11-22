package com.jpro.routing.filter.container

import javafx.beans.property.{ObjectProperty, Property}

trait Container {

  def contentProperty(): ObjectProperty[javafx.scene.Node]
  def getContent(): javafx.scene.Node = contentProperty().get()
  def setContent(x: javafx.scene.Node): Unit = contentProperty().set(x)

  def requestProperty(): ObjectProperty[com.jpro.routing.Request]
  def getRequest(): com.jpro.routing.Request = requestProperty().get()
  def setRequest(x: com.jpro.routing.Request): Unit = requestProperty().set(x)

}
