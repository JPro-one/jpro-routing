package one.jpro.auth.authentication;

import java.util.Optional;

/**
 * A runtime exception thrown when authentication fails.
 *
 * @author Besmir Beqiri
 */
public class AuthenticationException extends RuntimeException {

    private final AuthenticationResponse response;

    public AuthenticationException() {
        super();
        this.response = null;
    }

    public AuthenticationException(AuthenticationResponse response) {
        super(response.getMessage().orElse(null));
        this.response = response;
    }

    public AuthenticationException(String message) {
        super(message);
        response = null;
    }

    public Optional<AuthenticationResponse> getResponse() {
        return Optional.ofNullable(response);
    }
}
