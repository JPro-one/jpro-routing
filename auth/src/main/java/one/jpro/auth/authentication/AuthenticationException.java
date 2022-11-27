package one.jpro.auth.authentication;

/**
 * A runtime exception thrown when authentication fails.
 *
 * @author Besmir Beqiri
 */
public class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable ex) {
        super(message, ex);
    }
}
