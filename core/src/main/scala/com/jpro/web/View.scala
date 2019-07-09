package com.jpro.web

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
}

