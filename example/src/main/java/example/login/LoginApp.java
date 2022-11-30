package example.login;

import com.jpro.routing.Route;
import com.jpro.routing.RouteApp;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import one.jpro.auth.oath2.OAuth2Credentials;
import one.jpro.auth.oath2.provider.GoogleAuthenticationProvider;
import one.jpro.auth.oath2.provider.MicrosoftAuthenticationProvider;
import simplefx.experimental.parts.FXFuture;

import java.util.Arrays;

import static com.jpro.routing.RouteUtils.EmptyRoute;
import static com.jpro.routing.RouteUtils.getNode;

/**
 * Login example application.
 *
 * @author Besmir Beqiri
 */
public class LoginApp extends RouteApp {

    private static final String GOOGLE_CLIENT_ID = System.getenv("GOOGLE_TEST_CLIENT_ID");
    private static final String GOOGLE_CLIENT_SECRET = System.getenv("GOOGLE_TEST_CLIENT_SECRET");

    private static final String AZURE_CLIENT_ID = System.getenv("AZURE_TEST_CLIENT_ID");
    private static final String AZURE_CLIENT_SECRET = System.getenv("AZURE_TEST_CLIENT_SECRET");
    private static final String AZURE_TENANT = System.getenv("AZURE_TEST_CLIENT_TENANT");

    private final StringProperty nameProperty = new SimpleStringProperty(this, "name");
    private final StringProperty attributesProperty = new SimpleStringProperty(this, "attributes");

    public LoginApp() {
    }

    @Override
    public Route createRoute() {
        // Google Auth provider
        final var googleAuth = new GoogleAuthenticationProvider(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET);
        final var googleCredentials = new OAuth2Credentials()
                .scopes(Arrays.asList("openid", "email"))
                .redirectUri("http://localhost:8080/")
                .nonce("0394852-3190485-2490358");

        // Microsoft Auth provider
        System.out.println("AZURE_CLIENT_ID: " + AZURE_CLIENT_ID);
        System.out.println("AZURE_CLIENT_SECRET: " + AZURE_CLIENT_SECRET);
        final var microsoftAuth = new MicrosoftAuthenticationProvider(AZURE_CLIENT_ID, AZURE_CLIENT_SECRET, "common");
        final var microsoftCredentials = new OAuth2Credentials()
                .scopes(Arrays.asList("openid", "email"))
                .redirectUri("http://localhost:8080/");

        return EmptyRoute()
                .and(getNode("/", (r) -> {
                    var queryParam = r.queryParameters();
                    if (queryParam.isDefinedAt("code")) {
                        microsoftCredentials.code(queryParam.apply("code"));
//                        FXFuture.fromJava(googleAuth.authenticate(googleCredentials))
//                                        .map(authentication -> {
//                                    nameProperty.set("name: " + googleCredentials.getUsername());
//                                    attributesProperty.set("attributes: " + authentication.getAttributes());
//                                            return authentication;
//                                        });
                         var authFuture = microsoftAuth.authenticate(microsoftCredentials).thenAccept(authentication ->
                                Platform.runLater(() -> {
                                    nameProperty.set("name: " + microsoftCredentials.getUsername());
                                    attributesProperty.set("attributes: " + authentication.getAttributes());
                                }));

                         authFuture.exceptionally(ex -> {
                             System.out.println("LoginApp: " + ex.getMessage());
                            ex.printStackTrace();
                            return null;
                        });
                        return loginDataView();
                    } else {
                        final var googleAuthUrl = googleAuth.authorizeUrl(googleCredentials);
                        final var microsoftAuthUrl = microsoftAuth.authorizeUrl(microsoftCredentials);
                        System.out.println("microsoftAuthUrl: " + microsoftAuthUrl);
                        return initView(googleAuthUrl, microsoftAuthUrl);
                    }
                }));

    }

    public Node initView(String googleAuthUrl, String microsoftAuthUrl) {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(12.0);
        gridPane.setVgap(12.0);
        gridPane.setMaxWidth(600.0);
        gridPane.setPadding(new Insets(36.0));
        gridPane.setStyle("");

        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setMinWidth(80.0);
        columnConstraints1.setPrefWidth(80.0);
        columnConstraints1.setMaxWidth(Double.POSITIVE_INFINITY);
        columnConstraints1.setHalignment(HPos.RIGHT);

        ColumnConstraints columnConstraints2 = new ColumnConstraints();
        columnConstraints2.setMinWidth(120.0);
        columnConstraints2.setPrefWidth(120.0);
        columnConstraints2.setMaxWidth(Double.POSITIVE_INFINITY);
        columnConstraints2.setHgrow(Priority.ALWAYS);

        gridPane.getColumnConstraints().addAll(columnConstraints1, columnConstraints2);

        Label headerLabel = new Label("Login Form");
        headerLabel.setFont(Font.font("RobotoBold", 24.0));
        GridPane.setMargin(headerLabel, new Insets(20, 0, 20, 0));
        GridPane.setHalignment(headerLabel, HPos.CENTER);
        gridPane.add(headerLabel, 0, 0, 2, 1);

        Label usernameLabel = new Label("Username : ");
        gridPane.add(usernameLabel, 0, 2);
        TextField usernameField = new TextField();
        gridPane.add(usernameField, 1, 2);

        Label passwordLabel = new Label("Password : ");
        gridPane.add(passwordLabel, 0, 3);
        TextField passwordField = new TextField();
        gridPane.add(passwordField, 1, 3);

        Button submitButton = new Button("Login with JPro");
        submitButton.setDefaultButton(true);
        gridPane.add(submitButton, 0, 4, 2, 1);
        GridPane.setHalignment(submitButton, HPos.CENTER);
        GridPane.setMargin(submitButton, new Insets(12, 0, 12, 0));

        Button googleLoginButton = new Button("Login with Google");
        gridPane.add(googleLoginButton, 0, 5, 2, 1);
        GridPane.setMargin(googleLoginButton, new Insets(12, 0, 12, 0));
        GridPane.setHalignment(googleLoginButton, HPos.CENTER);
        googleLoginButton.setOnAction(event -> getWebAPI().openURL(googleAuthUrl));

        Button microsoftLoginButton = new Button("Login with Microsoft");
        gridPane.add(microsoftLoginButton, 0, 6, 2, 1);
        GridPane.setMargin(microsoftLoginButton, new Insets(12, 0, 12, 0));
        GridPane.setHalignment(microsoftLoginButton, HPos.CENTER);
        microsoftLoginButton.setOnAction(event -> getWebAPI().openURL(microsoftAuthUrl));

        return new StackPane(gridPane);
    }

    public Node loginDataView() {
        Label nameLabel = new Label();
        nameLabel.textProperty().bind(nameProperty);
        Label attributesLabel = new Label();
        attributesLabel.setWrapText(true);
        attributesLabel.textProperty().bind(attributesProperty);
        VBox pane = new VBox(nameLabel, attributesLabel);
        pane.setSpacing(4.0);
        pane.setStyle("-fx-background-color: limegreen");
        return pane;
    }
}
