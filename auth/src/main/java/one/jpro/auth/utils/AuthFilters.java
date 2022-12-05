package one.jpro.auth.utils;

import com.jpro.routing.Filter;
import one.jpro.auth.authentication.User;
import one.jpro.auth.oath2.OAuth2AuthenticationProvider;
import one.jpro.auth.oath2.OAuth2Credentials;
import simplefx.experimental.parts.FXFuture;

import java.util.function.Consumer;

/**
 * AuthFilters
 *
 * @author Besmir Beqiri
 */
public final class AuthFilters {

    public static Filter create(OAuth2AuthenticationProvider authProvider,
                                    OAuth2Credentials credentials,
                                    Consumer<User> consumer) {
        return (route) -> (request) -> {
            if (request.path().equals(credentials.getRedirectUri())) {
                return FXFuture.fromJava(authProvider.authenticate(credentials))
                        .map(user -> {
                            consumer.accept(user);
                            return user;
                        }).flatMap(user -> route.apply(request));
            } else {
                return route.apply(request);
            }
        };
    }

    private AuthFilters() {

    }
}
