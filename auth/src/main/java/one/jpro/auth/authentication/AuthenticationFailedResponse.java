package one.jpro.auth.authentication;

import java.util.Optional;

public class AuthenticationFailedResponse implements AuthenticationResponse {

    private final String message;

    public AuthenticationFailedResponse() {
        this(null);
    }

    public AuthenticationFailedResponse(String message) {
        this.message = message;
    }

    @Override
    public Optional<Authentication> getAuthentication() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

}
