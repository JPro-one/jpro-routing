package one.jpro.auth.authentication;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

/**
 * An implementation of the {@link Authentication} interface
 * to be used by clients.
 *
 * @author Besmir Beqiri
 */
public class ClientAuthentication implements Authentication {

    @Nonnull
    private final String name;

    @Nonnull
    private final Map<String, Object> attributes;

    public ClientAuthentication(@Nonnull String name,
                                @Nullable Map<String, Object> attributes) {
        this.name = name;
        this.attributes = attributes == null ? Collections.emptyMap() : attributes;
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    @Nonnull
    public Map<String , Object> getAttributes() {
        return new HashMap<>(attributes);
    }

    @Override
    @Nonnull
    public Collection<String> getRoles() {
        if (attributes != null) {
            Object rolesKey = attributes.get(KEY_ROLES);
            Object roleAttribute = attributes.get(rolesKey.toString());
            if (roleAttribute != null) {
                List<String> roles = new ArrayList<>();
                if (roleAttribute instanceof Iterable) {
                    //noinspection rawtypes
                    for (Object o : ((Iterable) roleAttribute)) {
                        roles.add(o.toString());
                    }
                } else {
                    roles.add(roleAttribute.toString());
                }
                return roles;
            }
        }

        return Collections.emptyList();
    }
}
