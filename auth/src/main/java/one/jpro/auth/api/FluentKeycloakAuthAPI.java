package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.oath2.provider.KeycloakAuthenticationProvider;
import org.json.JSONObject;

/**
 * Fluent Keycloak Authentication API.
 *
 * @author Besmir Beqiri
 */
public class FluentKeycloakAuthAPI implements FluentKeycloakAuth, FluentKeycloakAuth.FluentWebAPI {

    private WebAPI webAPI;
    private String site;
    private String clientId;
    private String realm;

    @Override
    public FluentKeycloakAuth webAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
        return this;
    }

    @Override
    public FluentKeycloakAuth site(String site) {
        this.site = site;
        return this;
    }

    @Override
    public FluentKeycloakAuth clientId(String clientId) {
        this.clientId = clientId;
        return null;
    }

    @Override
    public FluentKeycloakAuth realm(String realm) {
        this.realm = realm;
        return this;
    }

    @Override
    public KeycloakAuthenticationProvider create() {
        return new KeycloakAuthenticationProvider(webAPI, new JSONObject()
                .put("auth-server-url", site)
                .put("resource", clientId)
                .put("realm", realm));
    }
}
