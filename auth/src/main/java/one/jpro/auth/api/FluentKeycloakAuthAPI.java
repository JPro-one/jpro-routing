package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import javafx.stage.Stage;
import one.jpro.auth.oath2.OAuth2Flow;
import one.jpro.auth.oath2.provider.KeycloakAuthenticationProvider;
import org.json.JSONObject;

/**
 * Fluent Keycloak Authentication API.
 *
 * @author Besmir Beqiri
 */
public class FluentKeycloakAuthAPI implements FluentKeycloakAuth {

    private OAuth2Flow flow = OAuth2Flow.AUTH_CODE;
    private String site;
    private String clientId;
    private String clientSecret;
    private String realm;

    @Override
    public FluentKeycloakAuth flow(OAuth2Flow flow) {
        this.flow = flow;
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
        return this;
    }

    public FluentKeycloakAuth clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
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
            final JSONObject config = new JSONObject();
            if (site != null) config.put("auth-server-url", site);
            if (clientId != null) config.put("resource", clientId);
            if (clientSecret != null) config.put("credentials", new JSONObject().put("secret", clientSecret));
            if (realm != null) config.put("realm", realm);

            return new KeycloakAuthenticationProvider(WebAPI.getWebAPI(stage), flow, config);
        }
        else throw new UnsupportedOperationException("Keycloak authentication is currently supported " +
                "only when running the application via JPro server.");
    }
}
