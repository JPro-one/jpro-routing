package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.oath2.provider.KeycloakAuthenticationProvider;

/**
 * Fluent Keycloak Authentication interface.
 *
 * @author Besmir Beqiri
 */
public interface FluentKeycloakAuth {

    FluentKeycloakAuth site(String site);
    FluentKeycloakAuth clientId(String clientId);
    FluentKeycloakAuth realm(String realm);
    KeycloakAuthenticationProvider create();

    interface FluentWebAPI {
        FluentKeycloakAuth webAPI(WebAPI webAPI);
    }
}
