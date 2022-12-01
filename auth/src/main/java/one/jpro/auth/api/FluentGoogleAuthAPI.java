package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.oath2.provider.GoogleAuthenticationProvider;

/**
 * Fluent Google Authentication API.
 *
 * @author Besmir Beqiri
 */
public class FluentGoogleAuthAPI implements FluentGoogleAuth, FluentGoogleAuth.FluentWebAPI {

    private WebAPI webAPI;
    private String clientId;
    private String clientSecret;

    @Override
    public FluentGoogleAuth webAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
        return this;
    }

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
    public GoogleAuthenticationProvider create() {
        return new GoogleAuthenticationProvider(webAPI, clientId, clientSecret);
    }
}
