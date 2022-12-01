package one.jpro.auth.authentication;

import java.util.concurrent.CompletableFuture;

/**
 * Defines an authentication provider.
 *
 * @author Besmir Beqiri
 */
@FunctionalInterface
public interface AuthenticationProvider<T extends Credentials> {

    CompletableFuture<User> authenticate(T credentials);
}
