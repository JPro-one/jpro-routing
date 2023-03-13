package example.login;

import atlantafx.base.theme.Styles;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import one.jpro.auth.authentication.User;
import one.jpro.routing.RouteApp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Base class for all authentication applications.
 *
 * @author Besmir Beqiri
 */
public abstract class BaseAuthApp extends RouteApp {

    static final String GOOGLE_CLIENT_ID = System.getenv("GOOGLE_TEST_CLIENT_ID");
    static final String GOOGLE_CLIENT_SECRET = System.getenv("GOOGLE_TEST_CLIENT_SECRET");

    static final String AZURE_CLIENT_ID = System.getenv("AZURE_TEST_CLIENT_ID");
    static final String AZURE_CLIENT_SECRET = System.getenv("AZURE_TEST_CLIENT_SECRET");
//    private static final String AZURE_TENANT = System.getenv("AZURE_TEST_CLIENT_TENANT");

    /*                                                                                  */
    /*  Properties                                                                      */
    /*                                                                                  */
    /************************************************************************************/

    // User property
    private ObjectProperty<User> userProperty;

    final User getUser() {
        return userProperty == null ? null : userProperty.get();
    }

    final void setUser(User value) {
        userProperty().set(value);
    }

    final ObjectProperty<User> userProperty() {
        if (userProperty == null) {
            userProperty = new SimpleObjectProperty<>(this, "user");
        }
        return userProperty;
    }

    // Error property
    private ObjectProperty<Throwable> errorProperty;

    final Throwable getError() {
        return errorProperty == null ? null : errorProperty.get();
    }

    final void setError(Throwable value) {
        errorProperty().set(value);
    }

    final ObjectProperty<Throwable> errorProperty() {
        if (errorProperty == null) {
            errorProperty = new SimpleObjectProperty<>(this, "error");
        }
        return errorProperty;
    }

    /*                                                                                  */
    /*  Extra methods                                                                   */
    /*                                                                                  */
    /************************************************************************************/

    Button createLoginButton(String text) {
        ImageView iconView = new ImageView();
        iconView.setFitWidth(56);
        iconView.setFitHeight(56);
        Optional.ofNullable(getClass().getResourceAsStream("/images/" + text + "_Logo.png"))
                .map(inputStream -> new Image(inputStream, 0, 0, true, true))
                .ifPresent(iconView::setImage);

        Button loginButton = new Button("Login with\n" + text, iconView);
        loginButton.getStyleClass().addAll(Styles.BUTTON_OUTLINED, Styles.ACCENT, "login-button");
        return loginButton;
    }

    String jsonToMarkdown(JSONObject json) {
        return jsonToMarkdown(json, 0);
    }

    String jsonToMarkdown(JSONObject json, int level) {
        StringBuilder sb = new StringBuilder("\n");
        for (String key : json.keySet()) {
            final Object value = json.get(key);
            if (value instanceof JSONObject) {
                sb.append(" ".repeat(level * 4)).append("- ").append('`').append(key).append('`').append(": ")
                        .append(jsonToMarkdown((JSONObject) value, level + 1)).append("\n");
            } else if (value instanceof JSONArray) {
                sb.append(" ".repeat(level * 4)).append("- ").append('`').append(key).append('`').append(": ")
                        .append(jsonToMarkdown((JSONArray) value, level + 1)).append("\n");
            } else {
                sb.append(" ".repeat(level * 4)).append("- ").append('`').append(key).append('`').append(": ")
                        .append(value).append("\n");
            }
        }
        return sb.toString();
    }

    String jsonToMarkdown(JSONArray json, int level) {
        StringBuilder sb = new StringBuilder("\n");
        for (Object object : json) {
            if (object instanceof JSONObject) {
                sb.append(jsonToMarkdown((JSONObject) object, level + 1));
            } else if (object instanceof JSONArray) {
                sb.append(jsonToMarkdown((JSONArray) object, level + 1));
            } else {
                sb.append(" ".repeat(level * 4)).append("- ").append(object).append("\n");
            }
        }
        return sb.toString();
    }
}
