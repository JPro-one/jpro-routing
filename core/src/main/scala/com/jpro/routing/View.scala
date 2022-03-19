package com.jpro.routing

import com.jpro.routing.sessionmanager.SessionManager
import simplefx.all
import simplefx.core._
import simplefx.all._

abstract class View extends Response { THIS =>
  def title: String
  def description: String
  var url: String = null
  var isMobile: Boolean = false
  var sessionManager: SessionManager = null
  lazy val realContent: Node = content
  def content: Node
  def saveScrollPosition = true
  def fullscreen = false
  def onClose(): Unit = {}

  /**
   * Only overwrite this method, if you handle the url-change by yourself.
   * @param the path
   * @return whether the view handles the url change
   */
  def handleURL(x: String): Boolean = false
  def mapContent(f: Node => Node): View = new View {
    override def title: String = THIS.title

    override def description: String = THIS.description

    lazy val cachedContent = f(THIS.content)
    override def content: all.Node = cachedContent

    override def fullscreen: Boolean = THIS.fullscreen
  }
}

