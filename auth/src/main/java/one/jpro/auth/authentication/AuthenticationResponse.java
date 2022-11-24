package one.jpro.auth.authentication;

import jakarta.annotation.Nonnull;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * The response of an authentication attempt.
 *
 * @author Besmir Beqiri
 */
@FunctionalInterface
public interface AuthenticationResponse {

    /**
     * The user details if the response is authenticated.
     *
     * @return the optional {@link Authentication} object.
     */
    Optional<Authentication> getAuthentication();

    /**
     * If true, then the user is authenticated, else false.
     *
     * @return <code>true</code> then {@link #getAuthentication()}
     * method will return a non-empty optional.
     */
    default boolean isAuthenticated() {
        return getAuthentication().isPresent();
    }

    default Optional<String> getMessage() {
        return Optional.empty();
    }

    @Nonnull
    static AuthenticationResponse success(@Nonnull String username) {
        return AuthenticationResponse.success(username, Collections.emptyList(), Collections.emptyMap());
    }

    @Nonnull
    static AuthenticationResponse success(@Nonnull String username,
                                          @Nonnull Collection<String> roles) {
        return AuthenticationResponse.success(username, roles, Collections.emptyMap());
    }

    @Nonnull
    static AuthenticationResponse success(@Nonnull String username,
                                          @Nonnull Map<String, Object> attributes) {
        return AuthenticationResponse.success(username, Collections.emptyList(), attributes);
    }

    /**
     * A successful authentication response.
     *
     * @param username User's name.
     * @param roles User's roles.
     * @param attributes User's attributes.
     * @return a successful {@link AuthenticationResponse}
     */
    static AuthenticationResponse success(@Nonnull String username,
                                          @Nonnull Collection<String> roles,
                                          @Nonnull Map<String, Object> attributes) {
        return () -> Optional.of(Authentication.build(username, roles, attributes));
    }

    static AuthenticationResponse failure(@Nonnull String message) {
        return new AuthenticationFailedResponse(message);
    }

    static AuthenticationResponse failure() {
        return new AuthenticationFailedResponse();
    }

    static AuthenticationException exception(@Nonnull String message) {
        return new AuthenticationException(new AuthenticationFailedResponse(message));
    }

    static AuthenticationException exception() {
        return new AuthenticationException(new AuthenticationFailedResponse());
    }
}
