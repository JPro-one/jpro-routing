package  com.jpro.routing.dev

import simplefx.core._
import simplefx.all._
import org.scenicview.ScenicView
import com.jpro.routing.RouteUtils
import com.jpro.routing.Filter
import fr.brouillard.oss.cssfx.CSSFX
import com.jpro.routing.LinkUtil

object DevFilter {

  object DevFilterContainerFactory extends RouteUtils.SFXContainerFactory {
    override def createContainer() = new MyContainer
    class MyContainer extends VBox with Container {
  
      this <++ new HBox {
        this <++ new Button("<") {
          onAction --> {
            LinkUtil.goBack(this)
          }
        }
        this <++ new Button(">") {
          onAction --> {
            LinkUtil.goForward(this)
          }
        }
        this <++ new TextField {
          request --> {
            this.text = request.path
          }
          onAction --> {
            LinkUtil.gotoPage(this,getText())
          }
        }
        this <++ new Button("Scenic View") {
            onAction --> {
                ScenicView.show(this)
            }
        }
      }
      this <++ new StackPane {
        javafx.scene.layout.VBox.setVgrow(this, Priority.ALWAYS)
        children <-- (if(content != null) List(content) else Nil)
      }
    }
  }

  def createDevFilter(): Filter = {
    CSSFX.start()
    RouteUtils.containerFilter(DevFilterContainerFactory)
  }
}
