package com.jpro.web

import simplefx.core._
import simplefx.all._

trait NodeView extends View { this: Node =>
  override def content = this
}
