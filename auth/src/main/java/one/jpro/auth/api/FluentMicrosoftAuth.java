package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.oath2.provider.MicrosoftAuthenticationProvider;

/**
 * Fluent Microsoft Authentication interface.
 *
 * @author Besmir Beqiri
 */
public interface FluentMicrosoftAuth {

    FluentMicrosoftAuth clientId(String clientId);
    FluentMicrosoftAuth clientSecret(String clientSecret);
    FluentMicrosoftAuth tenant(String tenant);

    MicrosoftAuthenticationProvider create();

    interface FluentWebAPI {
        FluentMicrosoftAuth webAPI(WebAPI webAPI);
    }
}
