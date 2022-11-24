package one.jpro.auth.authentication;

/**
 * Represents a request to authenticate.
 *
 * @author Besmir Beqiri
 */
public interface AuthenticationRequest<T, S> {

    /**
     * @return the token in the request
     */
    T getIdentity();

    /**
     * @return the secret in the request
     */
    S getSecret();
}
