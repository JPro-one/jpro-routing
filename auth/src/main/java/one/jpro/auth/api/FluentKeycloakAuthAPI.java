package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import javafx.stage.Stage;
import one.jpro.auth.oath2.provider.KeycloakAuthenticationProvider;
import org.json.JSONObject;

/**
 * Fluent Keycloak Authentication API.
 *
 * @author Besmir Beqiri
 */
public class FluentKeycloakAuthAPI implements FluentKeycloakAuth {

    private String site;
    private String clientId;
    private String realm;

    @Override
    public FluentKeycloakAuth site(String site) {
        this.site = site;
        return this;
    }

    @Override
    public FluentKeycloakAuth clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public FluentKeycloakAuth realm(String realm) {
        this.realm = realm;
        return this;
    }

    @Override
    public KeycloakAuthenticationProvider create(Stage stage) {
        if (WebAPI.isBrowser()) {
            return new KeycloakAuthenticationProvider(WebAPI.getWebAPI(stage), new JSONObject()
                    .put("auth-server-url", site)
                    .put("resource", clientId)
                    .put("realm", realm));
        }
        else throw new UnsupportedOperationException("Keycloak authentication is currently supported " +
                "only when running the application via JPro server.");
    }
}
