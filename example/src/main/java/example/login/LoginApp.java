package example.login;

import atlantafx.base.theme.PrimerLight;
import com.sandec.mdfx.MarkdownView;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import one.jpro.auth.AuthAPI;
import one.jpro.auth.oath2.OAuth2Credentials;
import one.jpro.auth.oath2.provider.GoogleAuthenticationProvider;
import one.jpro.auth.oath2.provider.KeycloakAuthenticationProvider;
import one.jpro.auth.oath2.provider.MicrosoftAuthenticationProvider;
import one.jpro.routing.Route;
import one.jpro.routing.dev.DevFilter;
import simplefx.experimental.parts.FXFuture;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static one.jpro.routing.LinkUtil.gotoPage;
import static one.jpro.routing.RouteUtils.getNode;

/**
 * An example application to show how to use the Authorization module in general
 * combined with the Routing module and various supported authentication providers.
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
                .setRedirectUri(GOOGLE_REDIRECT_PATH)
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
                .setRedirectUri(MICROSOFT_REDIRECT_PATH);

        // Keycloak Auth provider
        final var keycloakAuth = AuthAPI.keycloakAuth()
                .webAPI(getWebAPI())
                .site("http://192.168.1.80:8080/realms/{realm}")
                .clientId("myclient")
                .realm("myrealm")
                .create();

        final var keycloakCredentials = new OAuth2Credentials()
                .setScopes(List.of("openid", "email"))
                .setRedirectUri(KEYCLOAK_REDIRECT_PATH);

        return Route.empty()
                .and(getNode("/", (r) ->
                        loginView(googleAuth, googleCredentials,
                                microsoftAuth, microsoftCredentials,
                                keycloakAuth, keycloakCredentials)))
                .and(getNode(GOOGLE_REDIRECT_PATH, (r) -> authInfoView()))
                .and(getNode(MICROSOFT_REDIRECT_PATH, (r) -> authInfoView()))
                .and(getNode(KEYCLOAK_REDIRECT_PATH, (r) -> authInfoView()))
                .and(getNode(AUTH_ERROR_PATH, (r) -> errorView()))
                .and(getNode(GOOGLE_PROVIDER_PATH, (r) -> authProviderView()))
                .and(getNode(MICROSOFT_PROVIDER_PATH, (r) -> authProviderView()))
                .and(getNode(KEYCLOAK_PROVIDER_PATH, (r) -> authProviderView()))
                .and(getNode(PROVIDER_DISCOVERY_PATH, (r) -> providerDiscoveryView()))
//                .filter(Filters.FullscreenFilter(true))
                .filter(DevFilter.createDevFilter())
                .filter(oauth2(googleAuth, googleCredentials, this::setUser, this::setError))
                .filter(oauth2(microsoftAuth, microsoftCredentials, this::setUser, this::setError))
                .filter(oauth2(keycloakAuth, keycloakCredentials, this::setUser, this::setError));
    }

    public Node loginView(GoogleAuthenticationProvider googleAuth,
                          OAuth2Credentials googleCredentials,
                          MicrosoftAuthenticationProvider microsoftAuth,
                          OAuth2Credentials microsoftCredentials,
                          KeycloakAuthenticationProvider keycloakAuth,
                          OAuth2Credentials keycloakCredentials) {
        final var headerLabel = new Label("Authentication Module");
        headerLabel.getStyleClass().add("header-label");

        final var selectLabel = new Label("Select an authentication provider:");
        selectLabel.getStyleClass().add("header2-label");

        final var googleLoginButton = createLoginButton("Google");
        googleLoginButton.setOnAction(event -> {
            setAuthProvider(googleAuth);
            setAuthCredentials(googleCredentials);
            gotoPage(googleLoginButton, GOOGLE_PROVIDER_PATH);
        });

        final var microsoftLoginButton = createLoginButton("Microsoft");
        microsoftLoginButton.setOnAction(event -> {
            setAuthProvider(microsoftAuth);
            setAuthCredentials(microsoftCredentials);
            gotoPage(microsoftLoginButton, MICROSOFT_PROVIDER_PATH);
        });

        final var keycloakLoginButton = createLoginButton("Keycloak");
        keycloakLoginButton.setOnAction(event -> {
            setAuthProvider(keycloakAuth);
            setAuthCredentials(keycloakCredentials);
            gotoPage(keycloakLoginButton, KEYCLOAK_PROVIDER_PATH);
        });

        final var tilePane = new TilePane(googleLoginButton, microsoftLoginButton, keycloakLoginButton);
        tilePane.getStyleClass().add("tile-pane");
        VBox.setVgrow(tilePane, Priority.ALWAYS);

        final var pane = new VBox(headerLabel, selectLabel, tilePane);
        pane.getStyleClass().add("login-pane");

        final var stackPane = new StackPane(pane);
        stackPane.getStyleClass().add("page");
        return stackPane;
    }

    public Node authProviderView() {
        final var headerLabel = new Label("Authentication Provider:");
        headerLabel.getStyleClass().add("header-label");
        headerLabel.textProperty().bind(providerNameBinding("Authentication Provider: ", authProviderProperty()));

        final var pane = new VBox(headerLabel);
        pane.getStyleClass().add("auth-provider-pane");

        Optional.ofNullable(getAuthProvider()).ifPresent(authProvider ->
                Optional.ofNullable(getAuthCredentials()).ifPresent(authCredentials -> {
                    final var authOptions = authProvider.getOptions();

                    final var siteLabel = new Label("Site:");
                    final var siteField = new TextField(authOptions.getSite());
                    pane.getChildren().addAll(siteLabel, siteField);

                    Optional.ofNullable(authOptions.getTenant()).ifPresent(tenant -> {
                        final var tenantLabel = new Label("Tenant:");
                        final var tenantField = new TextField(tenant);
                        pane.getChildren().addAll(tenantLabel, tenantField);
                    });

                    final var clientIdLabel = new Label("Client ID:");
                    final var clientIdField = new TextField(authOptions.getClientId());
                    pane.getChildren().addAll(clientIdLabel, clientIdField);

                    Optional.ofNullable(authOptions.getClientSecret()).ifPresent(clientSecret -> {
                        final var clientSecretLabel = new Label("Client Secret:");
                        final var clientSecretField = new TextField(clientSecret);
                        pane.getChildren().addAll(clientSecretLabel, clientSecretField);
                    });

                    final var scopesLabel = new Label("Scopes:");
                    final var scopesField = new TextField(String.join(", ", authCredentials.getScopes()));
                    pane.getChildren().addAll(scopesLabel, scopesField);

                    final var redirectUriLabel = new Label("Redirect URI:");
                    final var redirectUriField = new TextField(authCredentials.getRedirectUri());
                    pane.getChildren().addAll(redirectUriLabel, redirectUriField);

                    Optional.ofNullable(authCredentials.getNonce()).ifPresent(nonce -> {
                        final var nonceLabel = new Label("Nonce:");
                        final var nonceField = new TextField(nonce);
                        pane.getChildren().addAll(nonceLabel, nonceField);
                    });

                    final var signInBox = createButtonWithDescription(
                            "Sign in with the selected authentication provider.", "Sign In",
                            event -> getWebAPI().openURL(authProvider.authorizeUrl(authCredentials)));

                    final var discoveryBox = createButtonWithDescription(
                            "The OpenID Connect Discovery provides a client with configuration details.",
                            "Discovery", event ->
                                    FXFuture.fromJava(authProvider.discover())
                                            .map(provider -> {
                                                final var options = provider.getOptions();
                                                setAuthOptions(options);
                                                gotoPage(headerLabel, PROVIDER_DISCOVERY_PATH);
                                                return provider;
                                            })
                                            .recover(throwable -> {
                                                setError(throwable);
                                                gotoPage(headerLabel, AUTH_ERROR_PATH);
                                                return null;
                                            }));
                    pane.getChildren().addAll(signInBox, discoveryBox);
                }));

        return new StackPane(pane);
    }

    public Node providerDiscoveryView() {
        final var headerLabel = new Label("OpenID Provider Discovery:");
        headerLabel.getStyleClass().add("header-label");
        headerLabel.textProperty().bind(providerNameBinding("OpenID Provider Discovery: ", authProviderProperty()));

        MarkdownView providerDiscoveryView = new MarkdownView();
        providerDiscoveryView.getStylesheets().add("/com/sandec/mdfx/mdfx-default.css");
        providerDiscoveryView.mdStringProperty().bind(Bindings.createStringBinding(() -> {
            final var authOptions = getAuthOptions();
            return authOptions == null ? "" : jsonToMarkdown(authOptions.toJSON());
        }, authOptionsProperty()));

        final var pane = new VBox(headerLabel, providerDiscoveryView);
        pane.getStyleClass().add("openid-provider-discovery-pane");

        return new StackPane(pane);
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
