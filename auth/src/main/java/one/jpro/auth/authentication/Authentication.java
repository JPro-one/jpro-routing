package one.jpro.auth.authentication;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.json.JSONObject;

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

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
    static Authentication create(@Nonnull String username) {
        return Authentication.create(username, null, null);
    }

    static  Authentication create(@Nonnull String username,
                                  @Nonnull Collection<String> roles) {
        Objects.requireNonNull(roles, "User's roles are null.");
        return new User(username, roles, null);
    }

    /**
     * Build an {@link Authentication} instance foe the user.
     *
     * @param username User's name
     * @param attributes User's attributes
     * @return An {@link Authentication} for the user
     */
    @Nonnull
    static Authentication create(@Nonnull String username,
                                 @Nonnull Map<String, Object> attributes) {
        Objects.requireNonNull(attributes, "User's attributes are null.");
        return new User(username, null, attributes);
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
    static Authentication create(@Nonnull String username,
                                 @Nullable Collection<String> roles,
                                 @Nullable Map<String, Object> attributes) {
        return new User(username, roles, attributes);
    }

    /**
     * Builds an {@link Authentication} instance for the user from a {@link JSONObject}.
     *
     * @param json a {@link JSONObject} containing user's data.
     * @return An {@link Authentication} for the user
     */
    @Nonnull
    static User create(@Nonnull JSONObject json) {
        return new User(json);
    }
}
