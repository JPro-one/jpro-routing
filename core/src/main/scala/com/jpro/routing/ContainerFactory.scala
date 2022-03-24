package com.jpro.routing

import javafx.scene.Node

trait ContainerFactory {
  def isContainer(x: Node): Boolean

  def createContainer(): Node

  def setContent(c: Node, x: Node): Unit

  def getContent(c: Node): Node

  def setRequest(c: Node, r: Request): Unit
}

