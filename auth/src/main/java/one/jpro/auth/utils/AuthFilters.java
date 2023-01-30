package one.jpro.auth.utils;

import com.jpro.routing.Filter;
import com.jpro.routing.Route;
import one.jpro.auth.authentication.User;
import one.jpro.auth.oath2.OAuth2AuthenticationProvider;
import one.jpro.auth.oath2.OAuth2Credentials;
import simplefx.experimental.parts.FXFuture;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utility class with filters used in the routing process.
 *
 * @author Besmir Beqiri
 */
public final class AuthFilters {

    /**
     * Creates {@link Route} filter from a given {@link OAuth2AuthenticationProvider},
     * {@link OAuth2Credentials} and an operation a given user if the authentication
     * is successful.
     *
     * @param authProvider the OAuth2 authentication provider
     * @param credentials the OAuth2 credentials
     * @param userConsumer operation on the given user argument
     * @return a {@link Filter} object
     */
    public static Filter create(OAuth2AuthenticationProvider authProvider,
                                OAuth2Credentials credentials,
                                Consumer<User> userConsumer) {
        Objects.requireNonNull(authProvider, "auth provider can not be null");
        Objects.requireNonNull(credentials, "credentials can not be null");
        Objects.requireNonNull(userConsumer, "user consumer can not be null");
        return (route) -> (request) -> {
            if (request.path().equals(credentials.getRedirectUri())) {
                return FXFuture.fromJava(authProvider.authenticate(credentials))
                        .map(user -> {
                            userConsumer.accept(user);
                            return user;
                        })
                        .flatMap(user -> route.apply(request));
            } else {
                return route.apply(request);
            }
        };
    }

    private AuthFilters() {
        // Hide the default constructor.
    }
}
