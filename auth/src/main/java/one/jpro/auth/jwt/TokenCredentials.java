package one.jpro.auth.jwt;

import one.jpro.auth.authentication.AuthenticationProvider;
import one.jpro.auth.authentication.CredentialValidationException;
import one.jpro.auth.authentication.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Credentials used by an {@link AuthenticationProvider} that requires tokens,
 * such as OAuth2, to perform its authentication.
 *
 * @author Besmir Beqiri
 */
public class TokenCredentials implements Credentials {

    @NotNull
    private String token;
    @Nullable
    private List<String> scopes;

    public TokenCredentials(@NotNull final String token) {
        this.token = token;
    }

    // token credentials from json
    public TokenCredentials(@NotNull final JSONObject json) {
        // token
        this.token = json.getString("token");
        // scopes
        if (json.has("scope")) {
            addScopes(json.getString("scope"));
        } else if (json.has("scopes")) {
            final JSONArray scopes = json.getJSONArray("scopes");
            setScopes(scopes.toList().stream().map(Object::toString).collect(Collectors.toList()));
        }
    }

    @NotNull
    public String getToken() {
        return token;
    }

    public TokenCredentials setToken(@NotNull String token) {
        this.token = token;
        return this;
    }

    @Nullable
    public List<String> getScopes() {
        return scopes;
    }

    public TokenCredentials setScopes(List<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    public TokenCredentials addScopes(String... scopes) {
        if (this.scopes == null) {
            this.scopes = new ArrayList<>();
        }
        this.scopes.addAll(List.of(scopes));
        return this;
    }

    @Override
    public <V> void validate(V arg) throws CredentialValidationException {
        if (token == null || token.isBlank()) {
            throw new CredentialValidationException("token cannot be null or blank");
        }
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject json = new JSONObject();
        Optional.of(getToken()).ifPresent(token -> json.put("token", token));
        Optional.ofNullable(Stream.ofNullable(getScopes())
                        .collect(Collector.of(JSONArray::new, JSONArray::putAll, JSONArray::putAll)))
                .ifPresent(jsonArray -> json.put("scopes", jsonArray));
        return json;
    }

    @Override
    public String toHttpAuthorization() {
        return "Bearer " + token;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }
}
