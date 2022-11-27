package example.login;

import com.jpro.routing.Route;
import com.jpro.routing.RouteApp;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import one.jpro.auth.oath2.GoogleAuthenticationProvider;
import one.jpro.auth.oath2.OAuth2Credentials;

import static com.jpro.routing.RouteUtils.EmptyRoute;
import static com.jpro.routing.RouteUtils.getNode;

/**
 * Login example application.
 *
 * @author Besmir Beqiri
 */
public class LoginApp extends RouteApp {

    private static final String CLIENT_ID = "208149411725-neh4sbdegu1ds77t7pqijfbkf34f8gla.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "qc-m6BtLtzYovXZQRfRrlnbs";

    private final StringProperty emailProperty = new SimpleStringProperty(this, "email");
    private final StringProperty tokenProperty = new SimpleStringProperty(this, "token");

    public LoginApp() {
    }

    @Override
    public Route createRoute() {
        var googleAuth = new GoogleAuthenticationProvider(getWebAPI(), CLIENT_ID, CLIENT_SECRET);

        return EmptyRoute()
                .and(getNode("/", (r) -> {
                    var queryParam = r.queryParameters();
                    if (queryParam.isDefinedAt("code")) {
                        final var credentials = new OAuth2Credentials();
                        googleAuth.authenticate(credentials).thenAccept(authentication -> {
                            credentials.username(authentication.getName());
                            credentials.code(String.valueOf(authentication.getAttributes().get("access_token")));

                            Platform.runLater(() -> {
                                emailProperty.set("email: " + credentials.getUsername());
                                tokenProperty.set("token: " + credentials.getCode());
                            });
                        });
                        return loginDataView();
                    }
                    return initView(googleAuth.loginUrl());
                }));
//                .and(getNode("/login", (r) -> loginDataView("email: ", "token: ")));

    }

    public Node initView(String loginUrl) {
        var googleLoginButton = new Button("Login with Google");
        googleLoginButton.setOnAction(event -> getWebAPI().openURL(loginUrl));
        final var pane = new StackPane(googleLoginButton);
        pane.setStyle("-fx-background-color: azure;");
        return pane;
    }

    public Node loginDataView() {
        var emailLabel = new Label();
        emailLabel.textProperty().bind(emailProperty);
        var tokenLabel = new Label();
        tokenLabel.setWrapText(true);
        tokenLabel.textProperty().bind(tokenProperty);
        var pane = new VBox(emailLabel, tokenLabel);
        pane.setStyle("-fx-background-color: beige");
        return pane;
    }
}
