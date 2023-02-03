package one.jpro.auth.oath2;

import one.jpro.auth.authentication.AuthenticationProvider;
import one.jpro.auth.authentication.CredentialValidationException;
import one.jpro.auth.authentication.Credentials;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Credentials used by an {@link AuthenticationProvider} that requires tokens,
 * such as OAuth2, to perform its authentication.
 *
 * @author Besmir Beqiri
 */
public class TokenCredentials implements Credentials {

    private String token;
    private List<String> scopes;

    public TokenCredentials(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public TokenCredentials setToken(String token) {
        this.token = token;
        return this;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public TokenCredentials setScopes(List<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    public TokenCredentials setScopes(String... scopes) {
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
        Optional.ofNullable(getToken()).ifPresent(token -> json.put("token", token));
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
