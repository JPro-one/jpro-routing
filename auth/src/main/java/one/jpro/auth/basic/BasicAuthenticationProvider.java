package one.jpro.auth.basic;

import jakarta.inject.Singleton;
import one.jpro.auth.authentication.AuthenticationProvider;
import one.jpro.auth.authentication.AuthenticationRequest;
import one.jpro.auth.authentication.AuthenticationResponse;

import java.util.concurrent.CompletableFuture;

/**
 * Basic authentication provider for username and password.
 *
 * @author Besmir Beqiri
 */
@Singleton
public class BasicAuthenticationProvider implements AuthenticationProvider {

    // TODO: Implement a way to retrieve user data.

    public BasicAuthenticationProvider() {

    }

    @Override
    public CompletableFuture<AuthenticationResponse> authenticate(AuthenticationRequest<?, ?> authenticationRequest) {
        return CompletableFuture.supplyAsync(() -> {
            final var username = authenticationRequest.getIdentity();
            final var password = authenticationRequest.getSecret();

            if ("user".equals(username) && "password".equals(password)) {
                return AuthenticationResponse.success("user");
            } else {
                return AuthenticationResponse.failure();
            }
        });
    }
}
