package one.jpro.auth;

import one.jpro.auth.api.*;

/**
 * Auth API.
 *
 * @author Besmir Beqiri
 */
public interface AuthAPI {

    static FluentGoogleAuth.FluentWebAPI googleAuth() {
        return new FluentGoogleAuthAPI();
    }

    static FluentKeycloakAuth.FluentWebAPI keycloakAuth() {
        return new FluentKeycloakAuthAPI();
    }

    static FluentMicrosoftAuth.FluentWebAPI microsoftAuth() {
        return new FluentMicrosoftAuthAPI();
    }
}
