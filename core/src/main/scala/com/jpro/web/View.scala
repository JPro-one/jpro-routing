package com.jpro.web

import com.jpro.web.sessionmanager.SessionManager
import simplefx.core._
import simplefx.all._

abstract class View extends Result {
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
}

