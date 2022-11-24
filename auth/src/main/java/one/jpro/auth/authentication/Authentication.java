package one.jpro.auth.authentication;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Represents the state of an authentication.
 *
 * @author Besmir Beqiri
 */
public interface Authentication extends Principal {

    String KEY_NAME = "name";
    String KEY_ROLES = "roles";
    String KEY_ATTRIBUTES = "attributes";

    /**
     * Any additional attributes in the authentication.
     *
     * @return a {@link Map} containing the attributes;
     */
    @Nonnull
    Map<String, Object> getAttributes();

    /**
     * Any roles associated with the authentication.
     *
     * @return a {@link Collection} of roles as string
     */
    @Nonnull
    default Collection<String> getRoles() {
        return Collections.emptyList();
    }

    @Nonnull
    static Authentication build(@Nonnull String username) {
        return Authentication.build(username, null, null);
    }

    static  Authentication build(@Nonnull String username,
                                 @Nonnull Collection<String> roles) {
        return new ServerAuthentication(username, roles, null);
    }

    /**
     * Build an {@link Authentication} instance foe the user.
     *
     * @param username User's name
     * @param attributes User's attributes
     * @return An {@link Authentication} for the user
     */
    @Nonnull
    static Authentication build(@Nonnull String username,
                                @Nonnull Map<String, Object> attributes) {
        return new ServerAuthentication(username, null, attributes);
    }

    /**
     * Builds an {@link Authentication} instance for the user.
     *
     * @param username User's name
     * @param roles User's roles
     * @param attributes User's attributes
     * @return An {@link Authentication} for the user
     */
    @Nonnull
    static Authentication build(@Nonnull String username,
                                @Nullable Collection<String> roles,
                                @Nullable Map<String, Object> attributes) {
        return new ServerAuthentication(username, roles, attributes);
    }
}
