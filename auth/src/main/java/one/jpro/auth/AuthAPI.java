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

    static FluentMicrosoftAuth.FluentWebAPI microsoftAuth() {
        return new FluentMicrosoftAuthAPI();
    }
}
