 package com.jpro.routing

import org.junit.jupiter.api.Test

class TestLinkUtil {

  @Test
  def testValidLink(): Unit = {
    assert(LinkUtil.isValidLink("http://localhost:8081/d"))
  }

}
