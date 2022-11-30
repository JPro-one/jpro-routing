package one.jpro.auth.oath2;

import one.jpro.auth.authentication.CredentialValidationException;
import one.jpro.auth.authentication.Credentials;
import org.json.JSONObject;

import java.util.List;

/**
 * Credentials specific to the {@link OAuth2AuthenticationProvider}.
 *
 * @author Besmir Beqiri
 */
public class OAuth2Credentials implements Credentials {

    private String code;            // swap code for token
    private String codeVerifier;
    private String redirectUri;
    private JSONObject jwt;         // jwt-bearer tokens can include other kind of generic data
    private String assertion;       // or contain an assertion
    private String password;        // password credentials
    private String username;
    private List<String> scopes;    // control state
    private OAuth2Flow flow;
    private String nonce;

    /**
     * Default constructor.
     */
    public OAuth2Credentials() {
    }

    public String getCode() {
        return code;
    }

    public OAuth2Credentials code(String code) {
        this.code = code;
        return this;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public OAuth2Credentials codeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
        return this;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public OAuth2Credentials redirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
        return this;
    }

    public JSONObject getJwt() {
        return jwt;
    }

    public OAuth2Credentials jwt(JSONObject jwt) {
        this.jwt = jwt;
        return this;
    }

    public String getAssertion() {
        return assertion;
    }

    public OAuth2Credentials assertion(String assertion) {
        this.assertion = assertion;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public OAuth2Credentials password(String password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public OAuth2Credentials username(String username) {
        this.username = username;
        return this;
    }

    public List<String> getScopes() {
        return scopes;
    }

    public OAuth2Credentials scopes(List<String> scopes) {
        this.scopes = scopes;
        return this;
    }

    public OAuth2Flow getFlow() {
        return flow;
    }

    public OAuth2Credentials flow(OAuth2Flow flow) {
        this.flow = flow;
        return this;
    }

    public String getNonce() {
        return nonce;
    }

    public OAuth2Credentials nonce(String nonce) {
        this.nonce = nonce;
        return this;
    }

    @Override
    public <V> void validate(V arg) throws CredentialValidationException {
        OAuth2Flow flow = (OAuth2Flow) arg;
        if (flow == null) {
            throw new CredentialValidationException("flow cannot be null");
        }
        // when there's no access token, validation shall be performed according to each flow
        switch (flow) {
            case AUTH_CODE:
                if (code == null || code.length() == 0) {
                    throw new CredentialValidationException("code cannot be null or empty");
                }
                if (redirectUri != null && redirectUri.length() == 0) {
                    throw new CredentialValidationException("redirectUri cannot be empty");
                }
                break;
            case PASSWORD:
                if (username == null || username.length() == 0) {
                    throw new CredentialValidationException("username cannot be null or empty");
                }
                if (password == null || password.length() == 0) {
                    throw new CredentialValidationException("password cannot be null or empty");
                }
                break;
        }
    }
}
