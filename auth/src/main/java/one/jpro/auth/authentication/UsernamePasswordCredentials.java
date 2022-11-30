package one.jpro.auth.authentication;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

/**
 * Username and password credentials holder.
 *
 * @author Besmir Beqiri
 */
public class UsernamePasswordCredentials implements Credentials {

    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    @Nonnull
    @NotBlank
    private String username;

    @Nonnull
    @NotBlank
    private String password;

    /**
     * Empty constructor.
     */
    public UsernamePasswordCredentials() {
    }

    /**
     * Default constructor.
     *
     * @param username User's name.
     * @param password User's password.
     */
    public UsernamePasswordCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public <V> void validate(V arg) throws CredentialValidationException {
        if (username == null) {
            throw new CredentialValidationException("username cannot be null");
        }
        // passwords are allowed to be empty
        // for example this is used by basic auth
        if (password == null) {
            throw new CredentialValidationException("password cannot be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsernamePasswordCredentials that = (UsernamePasswordCredentials) o;
        return Objects.equals(username, that.username) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }

    @Override
    public String toHttpAuthorization() {
        final var result = new StringBuilder();

        if (username != null) {
            // RFC check
            if (username.indexOf(':') != -1) {
                throw new IllegalArgumentException("Username cannot contain ':'");
            }
            result.append(username);
        }

        result.append(':');

        if (password != null) {
            result.append(password);
        }

        return "Basic " + BASE64_ENCODER.encodeToString(result.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        Optional.ofNullable(getUsername()).ifPresent(username -> json.put("username", username));
        Optional.ofNullable(getPassword()).ifPresent(password -> json.put("password", password));
        return json;
    }
}
