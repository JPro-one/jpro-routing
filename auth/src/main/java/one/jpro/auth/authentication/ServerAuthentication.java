package one.jpro.auth.authentication;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.util.*;

/**
 * An implementation of the {@link Authentication} interface to
 * be used on the server side to create authentication objects
 * from user data.
 *
 * @author Besmir Beqiri
 */
public class ServerAuthentication implements Authentication {

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
    public ServerAuthentication(@Nonnull String name,
                                @Nullable Collection<String> roles,
                                @Nullable Map<String, Object> attributes) {
        this.name = name;
        this.roles = (roles == null || roles.isEmpty()) ? new ArrayList<>() : roles;
        this.attributes = attributes == null ? Collections.emptyMap() : attributes;
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

    public Map<String, Object> toJson() {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put(KEY_NAME, getName());
        Map<String, Object> jsonAttributes = new HashMap<>(getAttributes());
        jsonAttributes.putIfAbsent(KEY_ROLES, getRoles());
        jsonMap.put(KEY_ATTRIBUTES, jsonAttributes);
        return jsonMap;
    }
}
