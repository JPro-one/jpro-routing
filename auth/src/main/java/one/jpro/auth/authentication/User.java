package one.jpro.auth.authentication;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An implementation of the {@link Authentication} interface to
 * be used on the server side to create authentication objects
 * from user data.
 *
 * @author Besmir Beqiri
 */
public class User implements Authentication {

    @Nonnull
    @NotBlank
    private final String name;

    @Nonnull
    private final Collection<String> roles;

    @Nonnull
    private final Map<String, Object> attributes;

    /**
     * Create a server authentication holding user's name, roles and attributes.
     *
     * @param name The name the authenticated user
     * @param roles Roles of the authenticated user
     * @param attributes Attributes of the authenticated user
     */
    public User(@Nonnull String name,
                @Nullable Collection<String> roles,
                @Nullable Map<String, Object> attributes) {
        Objects.requireNonNull(name, "User's name is null.");
        this.name = name;
        this.roles = (roles == null || roles.isEmpty()) ? new ArrayList<>() : roles;
        this.attributes = attributes == null ? Collections.emptyMap() : attributes;
    }

    public User(@Nonnull JSONObject json) {
        String username = json.optString(KEY_NAME); // check with name key
        if (username == null || username.isBlank()) {
            username = json.optString("username"); // check with username key
            if (username == null || username.isBlank()) {
                throw new AuthenticationException("User's name is null.");
            }
        }

        name = username;

        if (json.has(KEY_ROLES)) {
            this.roles = json.getJSONArray(KEY_ROLES).toList().stream().map(Object::toString)
                    .collect(Collectors.toUnmodifiableList());
        } else {
            this.roles = new ArrayList<>();
        }

        if (json.has(KEY_ATTRIBUTES)) {
            this.attributes = json.getJSONObject(KEY_ATTRIBUTES).toMap();
        } else {
            this.attributes = Collections.emptyMap();
        }
    }

    @Override
    @Nonnull
    @NotBlank
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public Collection<String> getRoles() {
        return Collections.unmodifiableCollection(roles);
    }

    @Override
    @Nonnull
    public Map<String, Object> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put(KEY_NAME, getName());
        json.put(KEY_ROLES, getRoles());
        json.put(KEY_ATTRIBUTES, getAttributes());
        return json;
    }
}
