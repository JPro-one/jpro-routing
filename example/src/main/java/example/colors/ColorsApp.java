package example.colors;


import com.jpro.routing.*;
import com.jpro.routing.dev.DevFilter;
import com.jpro.routing.filter.container.ContainerFilter;
import example.filters.SimpleContainer;
import example.filters.SimpleHamburgerMenu;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.List;

import static com.jpro.routing.RouteUtils.redirect;
import static com.jpro.routing.RouteUtils.getNode;
import static com.jpro.routing.RouteUtils.EmptyRoute;

public class ColorsApp extends RouteApp {
  public static void main(String[] args) {
    launch(args);
  }

  public Route getRoute() {
    return EmptyRoute()
            .and(redirect("/", "/green"))
            //.and(CreateMDPages("/documentation1", "resources/something.md"))
            .and(getNode("/green", (r) -> gen("Green","/red", Color.GREEN)))
            .and(getNode("/red", (r) -> gen("Red", "/blue", Color.RED)))
            .and(getNode("/blue", (r) -> gen("Blue", "/yellow", Color.BLUE)))
            .and(getNode("/yellow", (r) -> gen("Yellow", "/red", Color.YELLOW)))
            .path("/colors",
                    EmptyRoute()
                            .and(getNode("/green", (r) -> gen("Green","./red", Color.GREEN)))
                            .and(getNode("/red", (r) -> gen("Red", "./green", Color.RED)))
            ).filter(Filters.FullscreenFilter(true))
            .filter(RouteUtils.sideTransitionFilter(1))
            .filter(DevFilter.createDevFilter())
            .filter(ContainerFilter.create(() -> new SimpleContainer()))
            .filter(ContainerFilter.create(() -> new SimpleHamburgerMenu(List.of(
                    new SimpleHamburgerMenu.Link("Green", "/green"),
                    new SimpleHamburgerMenu.Link("Red", "/red"),
                    new SimpleHamburgerMenu.Link("Blue", "/blue"),
                    new SimpleHamburgerMenu.Link("Yellow", "/yellow")
            ))));
  }

  public static Node gen(String title, String nextLink, Color color) {
    StackPane result = new StackPane();
    Label label = new Label(title);
    label.setStyle("-fx-font-size: 36px;");
    result.getChildren().add(label);
    result.setStyle("-fx-background-color: " + toHexString(color) + ";");

    StackPane linkArea = new StackPane();
    LinkUtil.setLink(linkArea, nextLink);
    result.getChildren().add(linkArea);
    return result;
  }
  public static String toHexString(Color value) {
    return "#" + (format(value.getRed()) + format(value.getGreen()) + format(value.getBlue()) + format(value.getOpacity())).toUpperCase();
  }
  private static String format(double v) {
    String in = Integer.toHexString((int) Math.round(v * 255));
    if (in.length() == 1) {
      return "0" + in;
    } else {
      return in;
    }
  }
  
}
