package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import javafx.stage.Stage;
import one.jpro.auth.oath2.provider.MicrosoftAuthenticationProvider;

/**
 * Fluent Microsoft Authentication API.
 *
 * @author Besmir Beqiri
 */
public class FluentMicrosoftAuthAPI implements FluentMicrosoftAuth {

    private String clientId;
    private String clientSecret;
    private String tenant;

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
    public MicrosoftAuthenticationProvider create(Stage stage) {
        if (WebAPI.isBrowser()) {
            return new MicrosoftAuthenticationProvider(WebAPI.getWebAPI(stage), clientId, clientSecret, tenant);
        }
        else throw new UnsupportedOperationException("Microsoft authentication is currently supported " +
                "only when running the application via JPro server.");
    }
}
