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
import one.jpro.auth.oath2.OAuth2AuthenticationProvider;
import one.jpro.auth.oath2.OAuth2Credentials;
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

        // Keycloak Auth provider
        final var keycloakAuth = AuthAPI.keycloakAuth()
                .webAPI(getWebAPI())
                .site("http://192.168.1.80:8080/realms/{realm}")
                .clientId("myclient")
                .realm("myrealm")
                .create();

        final var keycloakCredentials = new OAuth2Credentials()
                .setScopes(List.of("openid", "email"))
                .setRedirectUri("/auth/keycloak");

        return Route.empty()
                .and(getNode("/", (r) -> loginView()))
                .path("/auth", Route.empty()
                        .and(getNode("/google", (r) -> authInfoView()))
                        .and(getNode("/microsoft", (r) -> authInfoView()))
                        .and(getNode("/keycloak", (r) -> authInfoView())))
                .and(getNode(AUTH_ERROR_PATH, (r) -> errorView()))
                .path("/provider", Route.empty()
                        .and(getNode("/google", (r) -> authProviderView(googleAuth, googleCredentials)))
                        .and(getNode("/microsoft", (r) -> authProviderView(microsoftAuth, microsoftCredentials)))
                        .and(getNode("/keycloak", (r) -> authProviderView(keycloakAuth, keycloakCredentials)))
                        .path("/discovery", Route.empty()
                                .and(getNode("/google", (r) -> providerDiscoveryView(googleAuth)))
                                .and(getNode("/microsoft", (r) -> providerDiscoveryView(microsoftAuth)))
                                .and(getNode("/keycloak", (r) -> providerDiscoveryView(keycloakAuth)))))
//                .filter(Filters.FullscreenFilter(true))
                .filter(DevFilter.createDevFilter())
                .filter(oauth2(googleAuth, googleCredentials, this::setUser, this::setError))
                .filter(oauth2(microsoftAuth, microsoftCredentials, this::setUser, this::setError))
                .filter(oauth2(keycloakAuth, keycloakCredentials, this::setUser, this::setError));
    }

    public Node loginView() {
        final var headerLabel = new Label("Authentication Module");
        headerLabel.getStyleClass().add("header-label");

        final var selectLabel = new Label("Select an authentication provider:");
        selectLabel.getStyleClass().add("header2-label");

        final var googleProviderButton = createAuthProviderButton("Google");
        googleProviderButton.setOnAction(event -> gotoPage(googleProviderButton, GOOGLE_PROVIDER_PATH));

        final var microsoftProviderButton = createAuthProviderButton("Microsoft");
        microsoftProviderButton.setOnAction(event -> gotoPage(microsoftProviderButton, MICROSOFT_PROVIDER_PATH));

        final var keycloakProviderButton = createAuthProviderButton("Keycloak");
        keycloakProviderButton.setOnAction(event -> gotoPage(keycloakProviderButton, KEYCLOAK_PROVIDER_PATH));

        final var tilePane = new TilePane(googleProviderButton, microsoftProviderButton, keycloakProviderButton);
        tilePane.getStyleClass().add("tile-pane");
        VBox.setVgrow(tilePane, Priority.ALWAYS);

        final var pane = new VBox(headerLabel, selectLabel, tilePane);
        pane.getStyleClass().add("login-pane");

        final var stackPane = new StackPane(pane);
        stackPane.getStyleClass().add("page");
        return stackPane;
    }

    public Node authProviderView(final OAuth2AuthenticationProvider authProvider,
                                 final OAuth2Credentials authCredentials) {
        final var headerLabel = new Label("Authentication Provider: " + getAuthProviderName(authProvider));
        headerLabel.getStyleClass().add("header-label");

        final var pane = new VBox(headerLabel);
        pane.getStyleClass().add("auth-provider-pane");

        final var authOptions = authProvider.getOptions();

        final var siteLabel = new Label("Site:");
        final var siteField = new TextField(authOptions.getSite());
        siteField.setEditable(false);
        pane.getChildren().addAll(siteLabel, siteField);

        Optional.ofNullable(authOptions.getTenant()).ifPresent(tenant -> {
            final var tenantLabel = new Label("Tenant:");
            final var tenantField = new TextField(tenant);
            tenantField.setEditable(false);
            pane.getChildren().addAll(tenantLabel, tenantField);
        });

        final var clientIdLabel = new Label("Client ID:");
        final var clientIdField = new TextField(authOptions.getClientId());
        clientIdField.setEditable(false);
        pane.getChildren().addAll(clientIdLabel, clientIdField);

        Optional.ofNullable(authOptions.getClientSecret()).ifPresent(clientSecret -> {
            final var clientSecretLabel = new Label("Client Secret:");
            final var clientSecretField = new TextField(clientSecret);
            clientSecretField.setEditable(false);
            pane.getChildren().addAll(clientSecretLabel, clientSecretField);
        });

        final var scopesLabel = new Label("Scopes:");
        final var scopesField = new TextField(String.join(", ", authCredentials.getScopes()));
        scopesField.setEditable(false);
        pane.getChildren().addAll(scopesLabel, scopesField);

        final var redirectUriLabel = new Label("Redirect URI:");
        final var redirectUriField = new TextField(authCredentials.getRedirectUri());
        redirectUriField.setEditable(false);
        pane.getChildren().addAll(redirectUriLabel, redirectUriField);

        Optional.ofNullable(authCredentials.getNonce()).ifPresent(nonce -> {
            final var nonceLabel = new Label("Nonce:");
            final var nonceField = new TextField(nonce);
            nonceField.setEditable(false);
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
                                    gotoPage(headerLabel, "/provider/discovery/"
                                            + getAuthProviderName(authProvider).toLowerCase());
                                    return provider;
                                })
                                .recover(throwable -> {
                                    setError(throwable);
                                    gotoPage(headerLabel, AUTH_ERROR_PATH);
                                    return null;
                                }));
        pane.getChildren().addAll(signInBox, discoveryBox);

        return new StackPane(pane);
    }

    public Node providerDiscoveryView(final OAuth2AuthenticationProvider authProvider) {
        final var headerLabel = new Label("OpenID Connect Discovery: " + getAuthProviderName(authProvider));
        headerLabel.getStyleClass().add("header-label");

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
        final var headerLabel = new Label("Authentication information:");
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
