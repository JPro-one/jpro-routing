package one.jpro.auth.authentication;

import java.util.concurrent.Future;

/**
 * Defines an authentication provider
 *
 * @author Besmir Beqiri
 */
public interface AuthenticationProvider {

    Future<AuthenticationResponse> authenticate(AuthenticationRequest<?, ?> authenticationRequest);
}
