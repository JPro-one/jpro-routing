package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import javafx.stage.Stage;
import one.jpro.auth.oath2.provider.GoogleAuthenticationProvider;

/**
 * Fluent Google Authentication API.
 *
 * @author Besmir Beqiri
 */
public class FluentGoogleAuthAPI implements FluentGoogleAuth {

    private String clientId;
    private String clientSecret;

    @Override
    public FluentGoogleAuth clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public FluentGoogleAuth clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @Override
    public GoogleAuthenticationProvider create(Stage stage) {
        if (WebAPI.isBrowser()) {
            return new GoogleAuthenticationProvider(WebAPI.getWebAPI(stage), clientId, clientSecret);
        }
        else throw new UnsupportedOperationException("Keycloak authentication is currently supported " +
                "only when running the application via JPro server.");
    }
}
