package example.colors;

import com.jpro.routing.WebApp;
import com.jpro.routing.sessionmanager.SessionManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Starter extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        WebApp app = new ColorsApp(stage);
        Scene scene = new Scene(app, 1400,800);
        stage.setScene(scene);
        stage.show();
        app.start(SessionManager.getDefault(app,stage));
    }
  
}
