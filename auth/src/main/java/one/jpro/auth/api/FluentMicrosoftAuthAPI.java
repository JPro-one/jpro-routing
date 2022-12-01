package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.oath2.provider.MicrosoftAuthenticationProvider;

/**
 * Fluent Microsoft Authentication API.
 *
 * @author Besmir Beqiri
 */
public class FluentMicrosoftAuthAPI implements FluentMicrosoftAuth, FluentMicrosoftAuth.FluentWebAPI {

    private WebAPI webAPI;
    private String clientId;
    private String clientSecret;
    private String tenant;

    @Override
    public FluentMicrosoftAuth webAPI(WebAPI webAPI) {
        this.webAPI = webAPI;
        return this;
    }

    @Override
    public FluentMicrosoftAuth clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public FluentMicrosoftAuth clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @Override
    public FluentMicrosoftAuth tenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    @Override
    public MicrosoftAuthenticationProvider create() {
        return new MicrosoftAuthenticationProvider(webAPI, clientId, clientSecret, tenant);
    }
}
