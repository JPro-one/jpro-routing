package example.login;

import example.auth.AuthFilters;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import one.jpro.auth.AuthAPI;
import one.jpro.auth.authentication.User;
import one.jpro.auth.oath2.OAuth2Credentials;
import one.jpro.auth.oath2.provider.GoogleAuthenticationProvider;
import one.jpro.auth.oath2.provider.MicrosoftAuthenticationProvider;
import one.jpro.routing.Filters;
import one.jpro.routing.Route;
import one.jpro.routing.RouteApp;
import one.jpro.routing.dev.DevFilter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static one.jpro.routing.RouteUtils.getNode;

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
//    private static final String AZURE_TENANT = System.getenv("AZURE_TEST_CLIENT_TENANT");

    private final ObjectProperty<User> userProperty = new SimpleObjectProperty<>(this, "user");
    private final ObjectProperty<Throwable> errorProperty = new SimpleObjectProperty<>(this, "error");

    @Override
    public Route createRoute() {
        Optional.ofNullable(LoginApp.class.getResource("/style.css"))
                .map(URL::toExternalForm)
                .ifPresent(css -> getScene().getStylesheets().add(css));

        // Google Auth provider
        final var googleAuth = AuthAPI.googleAuth()
                .webAPI(getWebAPI())
                .clientId(GOOGLE_CLIENT_ID)
                .clientSecret(GOOGLE_CLIENT_SECRET)
                .create();

        final var googleCredentials = new OAuth2Credentials()
                .setScopes(List.of("openid", "email"))
                .setRedirectUri("/auth/google")
                .setNonce("0394852-3190485-2490358");

        // Microsoft Auth provider
        final var microsoftAuth = AuthAPI.microsoftAuth()
                .webAPI(getWebAPI())
                .clientId(AZURE_CLIENT_ID)
                .clientSecret(AZURE_CLIENT_SECRET)
                .tenant("common")
                .create();

        final var microsoftCredentials = new OAuth2Credentials()
                .setScopes(List.of("openid", "email"))
                .setRedirectUri("/auth/microsoft");

        return Route.empty()
                .and(getNode("/", (r) ->
                        loginView(googleAuth, googleCredentials,
                                microsoftAuth, microsoftCredentials)))
                .and(getNode("/auth/google", (r) -> authInfoView()))
                .and(getNode("/auth/microsoft", (r) -> authInfoView()))
                .and(getNode("/auth/error", (r) -> errorView()))
                .filter(Filters.FullscreenFilter(true))
                .filter(DevFilter.createDevFilter())
                .filter(AuthFilters.oauth2(googleAuth, googleCredentials, userProperty::set, errorProperty::set))
                .filter(AuthFilters.oauth2(microsoftAuth, microsoftCredentials, userProperty::set, errorProperty::set));
    }

    public Node loginView(GoogleAuthenticationProvider googleAuth,
                          OAuth2Credentials googleCredentials,
                          MicrosoftAuthenticationProvider microsoftAuth,
                          OAuth2Credentials microsoftCredentials) {
        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(12.0);
        gridPane.setVgap(12.0);
        gridPane.setMaxWidth(600.0);
        gridPane.setPadding(new Insets(36.0));

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
        googleLoginButton.setOnAction(event ->
                getWebAPI().openURL(googleAuth.authorizeUrl(googleCredentials)));

        Button microsoftLoginButton = new Button("Login with Microsoft");
        gridPane.add(microsoftLoginButton, 0, 6, 2, 1);
        GridPane.setMargin(microsoftLoginButton, new Insets(12, 0, 12, 0));
        GridPane.setHalignment(microsoftLoginButton, HPos.CENTER);
        microsoftLoginButton.setOnAction(event ->
                getWebAPI().openURL(microsoftAuth.authorizeUrl(microsoftCredentials)));

        final var stackPane = new StackPane(gridPane);
        stackPane.getStyleClass().add("page");
        return stackPane;
    }

    public Node authInfoView() {
        final var headerLabel = new Label("User information:");
        headerLabel.getStyleClass().add("header-label");

        Label nameLabel = new Label();
        nameLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            final var user = userProperty.get();
            return user == null ? "" : user.getName();
        }, userProperty));
        Label attributesLabel = new Label();
        attributesLabel.setWrapText(true);
        attributesLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            final var user = userProperty.get();
            return user == null ? "" : user.getAttributes().toString();
        }, userProperty));

        final var pane = new VBox(headerLabel, nameLabel, attributesLabel);
        pane.getStyleClass().add("auth-info-pane");

        return new StackPane(pane);
    }

    public Node errorView() {
        final var headerLabel = new Label("Something unexpected happen:");
        headerLabel.getStyleClass().add("header-label");

        final var errorLabel = new Label();
        errorLabel.setWrapText(true);
        errorLabel.getStyleClass().add("error-label");
        errorLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            final Throwable throwable = errorProperty.get();
            return throwable == null ? "" : throwable.getMessage();
        }, errorProperty));

        final var errorTextArea = new TextArea();
        errorTextArea.getStyleClass().add("error-text-area");
        VBox.setVgrow(errorTextArea, Priority.ALWAYS);
        errorTextArea.textProperty().bind(Bindings.createStringBinding(() -> {
            final Throwable throwable = errorProperty.get();
            if (throwable == null) {
                return "";
            } else {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                return sw.toString();
            }
        }, errorProperty));

        final var pane = new VBox(headerLabel, errorLabel, errorTextArea);
        pane.getStyleClass().add("error-pane");

        final var stackPane = new StackPane(pane);
        stackPane.getStyleClass().add("page");
        return stackPane;
    }
}
