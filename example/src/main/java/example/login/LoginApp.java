package example.login;

import atlantafx.base.theme.PrimerLight;
import com.sandec.mdfx.MarkdownView;
import example.auth.AuthFilters;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import one.jpro.auth.AuthAPI;
import one.jpro.auth.oath2.OAuth2Credentials;
import one.jpro.auth.oath2.provider.GoogleAuthenticationProvider;
import one.jpro.auth.oath2.provider.MicrosoftAuthenticationProvider;
import one.jpro.routing.Route;
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
public class LoginApp extends BaseAuthApp {

    @Override
    public Route createRoute() {
        Optional.ofNullable(LoginApp.class.getResource("/style.css"))
                .map(URL::toExternalForm)
                .ifPresent(css -> getScene().getStylesheets().add(css));
        getScene().setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

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
//                .filter(Filters.FullscreenFilter(true))
                .filter(DevFilter.createDevFilter())
                .filter(AuthFilters.oauth2(googleAuth, googleCredentials, this::setUser, this::setError))
                .filter(AuthFilters.oauth2(microsoftAuth, microsoftCredentials, this::setUser, this::setError));
    }

    public Node loginView(GoogleAuthenticationProvider googleAuth,
                          OAuth2Credentials googleCredentials,
                          MicrosoftAuthenticationProvider microsoftAuth,
                          OAuth2Credentials microsoftCredentials) {
        final var headerLabel = new Label("Authentication Module");
        headerLabel.getStyleClass().add("header-label");

        final var selectLabel = new Label("Select an authentication provider:");
        selectLabel.getStyleClass().add("header2-label");

        final var googleLoginButton = createLoginButton("Google");
        googleLoginButton.setOnAction(event ->
                getWebAPI().openURL(googleAuth.authorizeUrl(googleCredentials)));

        final var microsoftLoginButton = createLoginButton("Microsoft");
        microsoftLoginButton.setOnAction(event ->
                getWebAPI().openURL(microsoftAuth.authorizeUrl(microsoftCredentials)));

        final var tilePane = new TilePane(googleLoginButton, microsoftLoginButton);
        tilePane.getStyleClass().add("tile-pane");
        VBox.setVgrow(tilePane, Priority.ALWAYS);

        final var pane = new VBox(headerLabel, selectLabel, tilePane);
        pane.getStyleClass().add("login-pane");

        final var stackPane = new StackPane(pane);
        stackPane.getStyleClass().add("page");
        return stackPane;
    }

    public Node authInfoView() {
        final var headerLabel = new Label("User information:");
        headerLabel.getStyleClass().add("header-label");

        MarkdownView userView = new MarkdownView();
        userView.getStylesheets().add("/com/sandec/mdfx/mdfx-default.css");
        userView.mdStringProperty().bind(Bindings.createStringBinding(() -> {
            final var user = getUser();
            return user == null ? "" : jsonToMarkdown(user.toJSON());
        }, userProperty()));

        final var pane = new VBox(headerLabel, userView);
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
            final Throwable throwable = getError();
            return throwable == null ? "" : throwable.getMessage();
        }, errorProperty()));

        final var errorTextArea = new TextArea();
        errorTextArea.getStyleClass().add("error-text-area");
        VBox.setVgrow(errorTextArea, Priority.ALWAYS);
        errorTextArea.textProperty().bind(Bindings.createStringBinding(() -> {
            final Throwable throwable = getError();
            if (throwable == null) {
                return "";
            } else {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                throwable.printStackTrace(pw);
                return sw.toString();
            }
        }, errorProperty()));

        final var pane = new VBox(headerLabel, errorLabel, errorTextArea);
        pane.getStyleClass().add("error-pane");

        final var stackPane = new StackPane(pane);
        stackPane.getStyleClass().add("page");
        return stackPane;
    }
}
