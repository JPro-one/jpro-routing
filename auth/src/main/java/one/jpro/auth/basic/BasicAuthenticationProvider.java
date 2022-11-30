package one.jpro.auth.basic;

import one.jpro.auth.authentication.Authentication;
import one.jpro.auth.authentication.AuthenticationException;
import one.jpro.auth.authentication.AuthenticationProvider;
import one.jpro.auth.authentication.UsernamePasswordCredentials;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Basic authentication provider for username and password.
 *
 * @author Besmir Beqiri
 */
public class BasicAuthenticationProvider implements AuthenticationProvider<UsernamePasswordCredentials> {

    // TODO: Implement a way to store and retrieve user data.
    private final String username = "user";
    private final String password = "password";

    public BasicAuthenticationProvider() {
    }

    /**
     * If successful, returns an {@link Authentication} holding the authentication data, otherwise it fails.
     *
     * @param credentials user's credentials
     * @return an authentication response contained in a {@link Future} object.
     */
    @Override
    public CompletableFuture<Authentication> authenticate(UsernamePasswordCredentials credentials) {
        // validate the credentials before authentication
        credentials.validate(null);

        if (username.equals(credentials.getUsername()) && password.equals(credentials.getPassword())) {
            return CompletableFuture.completedFuture(Authentication.create(username));
        } else {
            return CompletableFuture.failedFuture(new AuthenticationException("Username and / or password is not correct."));
        }
    }
}
