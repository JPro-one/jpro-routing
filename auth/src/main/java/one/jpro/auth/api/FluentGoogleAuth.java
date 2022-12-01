package one.jpro.auth.api;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.oath2.provider.GoogleAuthenticationProvider;

/**
 * Fluent Google Authentication interface.
 *
 * @author Besmir Beqiri
 */
public interface FluentGoogleAuth {

    FluentGoogleAuth clientId(String clientId);
    FluentGoogleAuth clientSecret(String clientSecret);
    GoogleAuthenticationProvider create();

    interface FluentWebAPI {
        FluentGoogleAuth webAPI(WebAPI webAPI);
    }
}
