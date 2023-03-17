package one.jpro.auth.api;

import javafx.stage.Stage;
import one.jpro.auth.oath2.provider.KeycloakAuthenticationProvider;

/**
 * Fluent Keycloak Authentication interface.
 *
 * @author Besmir Beqiri
 */
public interface FluentKeycloakAuth {

    /**
     * Set the site.
     *
     * @param site the site
     * @return self
     */
    FluentKeycloakAuth site(String site);

    /**
     * Set the client id.
     *
     * @param clientId the client id
     * @return self
     */
    FluentKeycloakAuth clientId(String clientId);

    /**
     * Set the realm.
     *
     * @param realm the realm
     * @return self
     */
    FluentKeycloakAuth realm(String realm);

    /**
     * Create a Keycloak authentication provider configured with the provided options..
     *
     * @param stage the stage
     * @return a {@link KeycloakAuthenticationProvider} instance.
     */
    KeycloakAuthenticationProvider create(Stage stage);
}
