package one.jpro.auth.authentication;

import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Username and password credentials holder.
 *
 * @author Besmir Beqiri
 */
public class UsernamePasswordCredentials implements Credentials, AuthenticationRequest<String, String> {

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
    public String getIdentity() {
        return getUsername();
    }

    @Override
    public String getSecret() {
        return getPassword();
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
}
