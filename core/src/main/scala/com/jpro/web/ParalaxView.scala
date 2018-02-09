package com.jpro.web

import java.net.URL

import com.jpro.webapi.{HTMLView, WebAPI}
import simplefx.all._
import simplefx.core._

class ParalaxView(imgurl: URL) extends StackPane { THIS =>

  @Bind var image: Image = new Image(imgurl.toString)

  private var webAPI: WebAPI = null

  val id = "mynodeid_" + random[Int]

  def backgroundSize = "background-size:100% 200%;"

  updated {
    onceWhen(scene != null) --> {
      webAPI = WebAPI.getWebAPI(this.scene)

      this <++ new HTMLView {
        @Bind var content: String = contentProperty().toBindable

        content <-- {
          s"""<div id="$id" style="width: 100%;
             |height: 100%;
             |-webkit-transform: translate3d(0, 0, 0);
             |transform: translate3d(0, 0, 0);
             |background-repeat:no-repeat;
             |${backgroundSize}
             |background-image: url('${webAPI.createPublicFile(imgurl)}'); "></div>
             |""".stripMargin
        }

        // https://jsfiddle.net/nqaq4vz1/
        /**
          *   console.log("scrollPosition: " + scrollPosition);
          *   console.log("nodeHeight: " + nodeHeight);
          *   console.log("screenHeight: " + screenHeight);
          *   console.log("nodeY: " + nodeY);
          *
          */
        webAPI.executeScript(
          s"""(function() {
            |var update = (function() {
            |   var x = document.getElementById("${id}");
            |   var nodeHeight = x.getBoundingClientRect().height;
            |   var nodeY = x.getBoundingClientRect().top;
            |      console.log(window);
            |   var screenHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);
            |   var doc = document.documentElement;
            |   var scrollPosition = (window.pageYOffset || doc.scrollTop)  - (doc.clientTop || 0);
            |
            |   var min = -nodeHeight;
            |   var max = screenHeight;
            |   var dif = max - min;
            |   var perc = (nodeY - min) / dif
            |   var perc2 = Math.max(0.0, Math.min(perc,1.0)) * 100;
            |
            |   console.log("perc2: " + perc2);
            |
            |   x.style.backgroundPosition = ("50% " + perc2 + "%");
            |});
            |window.addEventListener("scroll", update);
            |update();
            |})();
          """.stripMargin)
      }

    }
  }


}
