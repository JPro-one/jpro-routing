package one.jpro.auth.oath2;

import org.json.JSONObject;

import java.net.http.HttpClient;
import java.util.List;

/**
 * Options describing how an OAuth2 {@link HttpClient} will make connection.
 *
 * @author Besmir Beqiri
 */
public class OAuth2Options {

    // Defaults
    private static final OAuth2Flow FLOW = OAuth2Flow.AUTH_CODE;
    private static final String AUTHORIZATION_PATH = "/oauth/authorize";
    private static final String TOKEN_PATH = "/oauth/token";
    private static final String REVOCATION_PATH = "/oauth/revoke";
    private static final JWTOptions JWT_OPTIONS = new JWTOptions();
    private static final String SCOPE_SEPARATOR = " ";
    private static final boolean VALIDATE_ISSUER = true;
    private static final long JWK_DEFAULT_AGE = -1L; // seconds of JWK default age (-1 means no rotation)

    private OAuth2Flow flow;
    private List<String> supportedGrantTypes;
    private String authorizationPath;
    private String tokenPath;
    private String revocationPath;
    private String scopeSeparator;
    // this is an openid-connect extension
    private boolean validateIssuer;
    private String logoutPath;
    private String userInfoPath;
    // extra parameters to be added while requesting the user info
    private JSONObject userInfoParams;
    // introspection RFC7662
    private String introspectionPath;
    // JWK path RFC7517
    private String jwkPath;
    //seconds of JWKs lifetime
    private long jwkMaxAge;
    // OpenID non standard
    private String tenant;

    private String site;
    private String clientId;
    private String clientSecret;

    //https://tools.ietf.org/html/rfc7521
    private String clientAssertionType;
    private String clientAssertion;

    private String userAgent;
    private JSONObject headers;
    private List<PubSecKeyOptions> pubSecKeys;
    private JWTOptions jwtOptions;
    // extra parameters to be added while requesting a token
    private JSONObject extraParams;

    public OAuth2Options() {
        flow = FLOW;
        validateIssuer = VALIDATE_ISSUER;
        authorizationPath = AUTHORIZATION_PATH;
        tokenPath = TOKEN_PATH;
        revocationPath = REVOCATION_PATH;
        scopeSeparator = SCOPE_SEPARATOR;
        jwtOptions = JWT_OPTIONS;
        jwkMaxAge = JWK_DEFAULT_AGE;
    }

    public OAuth2Flow getFlow() {
        return flow;
    }

    public OAuth2Options setFlow(OAuth2Flow flow) {
        this.flow = flow;
        return this;
    }

    public List<String> getSupportedGrantTypes() {
        return supportedGrantTypes;
    }

    public OAuth2Options setSupportedGrantTypes(List<String> supportedGrantTypes) {
        this.supportedGrantTypes = supportedGrantTypes;
        return this;
    }

    public String getAuthorizationPath() {
        return getCompletePath(authorizationPath);
    }

    public OAuth2Options setAuthorizationPath(String authorizationPath) {
        this.authorizationPath = authorizationPath;
        return this;
    }

    public String getTokenPath() {
        return getCompletePath(tokenPath);
    }

    public OAuth2Options setTokenPath(String tokenPath) {
        this.tokenPath = tokenPath;
        return this;
    }

    public String getRevocationPath() {
        return getCompletePath(revocationPath);
    }

    public OAuth2Options setRevocationPath(String revocationPath) {
        this.revocationPath = revocationPath;
        return this;
    }

    public String getScopeSeparator() {
        return scopeSeparator;
    }

    public OAuth2Options setScopeSeparator(String scopeSeparator) {
        this.scopeSeparator = scopeSeparator;
        return this;
    }

    public boolean isValidateIssuer() {
        return validateIssuer;
    }

    public OAuth2Options setValidateIssuer(boolean validateIssuer) {
        this.validateIssuer = validateIssuer;
        return this;
    }

    public String getLogoutPath() {
        return logoutPath;
    }

    public OAuth2Options setLogoutPath(String logoutPath) {
        this.logoutPath = logoutPath;
        return this;
    }

    public String getUserInfoPath() {
        return getCompletePath(userInfoPath);
    }

    public OAuth2Options setUserInfoPath(String userInfoPath) {
        this.userInfoPath = userInfoPath;
        return this;
    }

    public JSONObject getUserInfoParams() {
        return userInfoParams;
    }

    public OAuth2Options setUserInfoParams(JSONObject userInfoParams) {
        this.userInfoParams = userInfoParams;
        return this;
    }

    public String getIntrospectionPath() {
        return getCompletePath(introspectionPath);
    }

    public OAuth2Options setIntrospectionPath(String introspectionPath) {
        this.introspectionPath = introspectionPath;
        return this;
    }

    public String getJwkPath() {
        return getCompletePath(jwkPath);
    }

    public OAuth2Options setJwkPath(String jwkPath) {
        this.jwkPath = jwkPath;
        return this;
    }

    public long getJwkMaxAge() {
        return jwkMaxAge;
    }

    public OAuth2Options setJwkMaxAge(long jwkMaxAge) {
        this.jwkMaxAge = jwkMaxAge;
        return this;
    }

    public String getTenant() {
        return tenant;
    }

    public OAuth2Options setTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    public String getSite() {
        return site;
    }

    public OAuth2Options setSite(String site) {
        this.site = site;
        return this;
    }

    public String getClientId() {
        return clientId;
    }

    public OAuth2Options setClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public OAuth2Options setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public String getClientAssertionType() {
        return clientAssertionType;
    }

    public OAuth2Options setClientAssertionType(String clientAssertionType) {
        this.clientAssertionType = clientAssertionType;
        return this;
    }

    public String getClientAssertion() {
        return clientAssertion;
    }

    public OAuth2Options setClientAssertion(String clientAssertion) {
        this.clientAssertion = clientAssertion;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public OAuth2Options setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public JSONObject getHeaders() {
        return headers;
    }

    public OAuth2Options setHeaders(JSONObject headers) {
        this.headers = headers;
        return this;
    }

    public List<PubSecKeyOptions> getPubSecKeys() {
        return pubSecKeys;
    }

    public OAuth2Options setPubSecKeys(List<PubSecKeyOptions> pubSecKeys) {
        this.pubSecKeys = pubSecKeys;
        return this;
    }

    public JWTOptions getJwtOptions() {
        return jwtOptions;
    }

    public OAuth2Options setJwtOptions(JWTOptions jwtOptions) {
        this.jwtOptions = jwtOptions;
        return this;
    }

    public JSONObject getExtraParams() {
        return extraParams;
    }

    public OAuth2Options setExtraParams(JSONObject extraParams) {
        this.extraParams = extraParams;
        return this;
    }

    private String getCompletePath(String path) {
        if (path != null && path.charAt(0) == '/') {
            if (site != null && site.contains("{tenant}")) {
                if (tenant != null && !tenant.isBlank()) {
                    site = site.replace("{tenant}", tenant);
                } else {
                    throw new IllegalArgumentException("The tenant value is null or blank.");
                }
            }
            return site + path;
        }
        return path;
    }
}
