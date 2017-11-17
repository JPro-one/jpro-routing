package com.jpro.web

import simplefx.core._
import simplefx.all._

trait View {
  def title: String
  var url: String = null
  def content: Node
}

