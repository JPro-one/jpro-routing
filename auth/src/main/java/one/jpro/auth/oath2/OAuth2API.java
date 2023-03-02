package one.jpro.auth.oath2;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jpro.webapi.WebAPI;
import one.jpro.auth.authentication.AuthenticationException;
import one.jpro.auth.jwt.JWTOptions;
import one.jpro.auth.utils.HttpMethod;
import one.jpro.auth.utils.AuthUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static one.jpro.auth.utils.AuthUtils.*;

/**
 * OAuth2 API class.
 *
 * @author Besmir Beqiri
 */
public class OAuth2API {

    private static final Pattern MAX_AGE = Pattern.compile("max-age=\"?(\\d+)\"?");
    private static final String CACHE_CONTROL = "cache-control";
    private static final Base64.Encoder BASE64_ENCODER = AuthUtils.BASE64_ENCODER;

    @NotNull
    private final OAuth2Options options;
    @NotNull
    private final HttpClient httpClient;

    public OAuth2API(@NotNull final OAuth2Options options) {
        this.options = options;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * The client sends the end-user's browser to this endpoint to request their authentication and consent.
     * This endpoint is used in the code and implicit OAuth 2.0 flows which require end-user interaction.
     *
     * @param credentials the credentials to be used to authorize the user.
     * @return the url to be used to authorize the user.
     * @see <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
     */
    public String authorizeURL(OAuth2Credentials credentials) {
        final JSONObject query = credentials.toJSON();

        final OAuth2Flow flow;
        if (query.has("flow") && !query.getString("flow").isBlank()) {
            flow = OAuth2Flow.getFlow(query.getString("flow"));
        } else {
            flow = options.getFlow();
        }

        if (flow == OAuth2Flow.AUTH_CODE) {
            query.put("response_type", "code");
        }
//        else {
//            throw new IllegalStateException("authorization URL cannot be computed for non AUTH_CODE flow");
//        }

        if (query.has("scopes")) {
            // scopes have been passed as a list so the provider must generate the correct string for it
            query.put("scope", query.getJSONArray("scopes").join(options.getScopeSeparator())
                    .replace("\"", ""));
            query.remove("scopes");
        }

        final String clientId = options.getClientId();
        if (clientId != null) {
            query.put("client_id", clientId);
        } else {
            if (options.getClientAssertionType() != null) {
                query.put("client_assertion_type", options.getClientAssertionType());
            }
            if (options.getClientAssertion() != null) {
                query.put("client_assertion", options.getClientAssertion());
            }
        }

        return options.getAuthorizationPath() + '?' + jsonToQuery(query);
    }

    /**
     * Post an OAuth 2.0 grant (code, refresh token, resource owner password credentials, client credentials)
     * to obtain an ID and / or access token.
     *
     * @param grantType the grant type.
     * @param params    the parameters to be sent.
     * @see <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
     */
    public CompletableFuture<JSONObject> token(String grantType, JSONObject params) {
        if (grantType == null) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Token request requires a grantType other than null"));
        }

        final JSONObject headers = new JSONObject();

        final boolean confidentialClient = options.getClientId() != null && options.getClientSecret() != null;
        if (confidentialClient) {
            String basic = options.getClientId() + ":" + options.getClientSecret();
            headers.put("Authorization", "Basic " +
                    BASE64_ENCODER.encodeToString(basic.getBytes(StandardCharsets.UTF_8)));
        }

        // Send authorization params in the body
        final JSONObject form = new JSONObject(params.toString());
        if (options.getExtraParams() != null) {
            for (String key : JSONObject.getNames(options.getExtraParams())) {
                form.put(key, options.getExtraParams().get(key));
            }
        }

        form.put("grant_type", grantType);

        if (!confidentialClient) {
            final String clientId = options.getClientId();
            if (clientId != null) {
                form.put("client_id", clientId);
            } else {
                if (options.getClientAssertionType() != null) {
                    form.put("client_assertion_type", options.getClientAssertionType());
                }
                if (options.getClientAssertion() != null) {
                    form.put("client_assertion", options.getClientAssertion());
                }
            }
        }

        headers.put("Content-Type", "application/x-www-form-urlencoded");
        final String payload = jsonToQuery(form);

        // specify preferred accepted content type
        headers.put("Accept", "application/json,application/x-www-form-urlencoded;q=0.9");

        return fetch(HttpMethod.POST, options.getTokenPath(), headers, payload)
                .thenCompose(response -> {
                    if (response.body() == null || response.body().length() == 0) {
                        return CompletableFuture.failedFuture(new RuntimeException("No Body"));
                    }

                    JSONObject json;
                    final var header = response.headers();
                    if (containsValue(header, "application/json")) {
                        json = new JSONObject(response.body());
                    } else if (containsValue(header, "application/x-www-form-urlencoded")
                            || containsValue(header, "text/plain")) {
                        json = queryToJson(response.body());
                    } else {
                        return CompletableFuture.failedFuture(
                                new RuntimeException("Cannot handle content type: "
                                        + header.map().get("Content-Type")));
                    }

                    if (json == null || json.has("error")) {
                        return CompletableFuture.failedFuture(
                                new RuntimeException(extractErrorDescription(json)));
                    } else {
                        processNonStandardHeaders(json, response, options.getScopeSeparator());
                        return CompletableFuture.completedFuture(json);
                    }
                });
    }

    /**
     * Validate an access token and retrieve its underlying authorisation (for resource servers).
     *
     * @param tokenType the type of the token to be introspected.
     * @param token     the token to be introspected.
     * @see <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
     */
    public CompletableFuture<JSONObject> tokenIntrospection(String tokenType, String token) {
        final JSONObject headers = new JSONObject();

        final boolean confidentialClient = options.getClientId() != null && options.getClientSecret() != null;

        if (confidentialClient) {
            String basic = options.getClientId() + ":" + options.getClientSecret();
            headers.put("Authorization", "Basic " + BASE64_ENCODER.encodeToString(basic.getBytes(StandardCharsets.UTF_8)));
        }

        final JSONObject form = new JSONObject()
                .put("token", token)
                // optional param from RFC7662
                .put("token_type_hint", tokenType);

        headers.put("Content-Type", "application/x-www-form-urlencoded");
        final String payload = jsonToQuery(form);
        // specify preferred accepted accessToken type
        headers.put("Accept", "application/json,application/x-www-form-urlencoded;q=0.9");

        return fetch(HttpMethod.POST, options.getIntrospectionPath(), headers, payload)
                .thenCompose(response -> {
                    if (response.body() == null || response.body().length() == 0) {
                        return CompletableFuture.failedFuture(new RuntimeException("No Body"));
                    }

                    JSONObject json;

                    if (containsValue(response.headers(), "application/json")) {
                        json = new JSONObject(response.body());
                    } else if (containsValue(response.headers(), "application/x-www-form-urlencoded") ||
                            containsValue(response.headers(), "text/plain")) {
                        json = queryToJson(response.body());
                    } else return CompletableFuture.failedFuture(
                            new RuntimeException("Cannot handle accessToken type: "
                                    + response.headers().allValues("Content-Type")));

                    if (json == null || json.has("error")) {
                        return CompletableFuture.failedFuture(
                                new RuntimeException(extractErrorDescription(json)));
                    } else {
                        processNonStandardHeaders(json, response, options.getScopeSeparator());
                        return CompletableFuture.completedFuture(json);
                    }
                });
    }

    /**
     * Revoke an obtained access or refresh token.
     *
     * @param tokenType the type of the token to be revoked.
     * @param token     the token to be revoked.
     * @see <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
     */
    public CompletableFuture<Void> tokenRevocation(String tokenType, String token) {
        if (token == null) {
            return CompletableFuture.failedFuture(new RuntimeException("Cannot revoke null token"));
        }

        final JSONObject headers = new JSONObject();
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        final boolean confidentialClient = options.getClientId() != null && options.getClientSecret() != null;
        if (confidentialClient) {
            String basic = options.getClientId() + ":" + options.getClientSecret();
            headers.put("Authorization", "Basic " + BASE64_ENCODER.encodeToString(basic.getBytes(StandardCharsets.UTF_8)));
        }

        final JSONObject form = new JSONObject();
        form.put("token", token).put("token_type_hint", tokenType);
        final String payload = jsonToQuery(form);
        // specify preferred accepted accessToken type
        headers.put("Accept", "application/json,application/x-www-form-urlencoded;q=0.9");

        return fetch(HttpMethod.POST, options.getRevocationPath(), headers, payload)
                .thenCompose(response -> {
                    if (response.body() == null) {
                        return CompletableFuture.failedFuture(new RuntimeException("No Body"));
                    }

                    return CompletableFuture.completedFuture(null); // Void type
                });
    }

    /**
     * Retrieve user information and other attributes for a logged-in end-user.
     *
     * @param accessToken the access token
     * @return the user information wrapped in a JSON object
     */
    public CompletableFuture<JSONObject> userInfo(String accessToken) {
        final JSONObject headers = new JSONObject();
        final JSONObject extraParams = options.getExtraParams();
        String path = options.getUserInfoPath();

        if (path == null) {
            return CompletableFuture.failedFuture(new AuthenticationException("userInfo path is not configured"));
        }

        if (extraParams != null) {
            path += "?" + jsonToQuery(extraParams);
        }

        headers.put("Authorization", "Bearer " + accessToken);
        // specify preferred accepted accessToken type
        headers.put("Accept", "application/json,application/jwt,application/x-www-form-urlencoded;q=0.9");

        return fetch(HttpMethod.GET, path, headers, null)
                .thenCompose(response -> {
                    String body = response.body();

                    if (body == null) { // TODO: check if it's possible to have an empty body (body.length() == 0)
                        return CompletableFuture.failedFuture(new AuthenticationException("No Body"));
                    }

                    // userInfo is expected to be a JWT
                    JSONObject userInfo;
                    if (containsValue(response.headers(), "application/json")) {
                        userInfo = new JSONObject(body);
                    } else if (containsValue(response.headers(), "applications/jwt")) {
                        // userInfo is expected to be a JWT
                        final DecodedJWT decodedJWT = JWT.decode(body);
                        final JSONObject jwtHeader = new JSONObject(decodedJWT.getHeader());
                        final JSONObject jwtPayload = new JSONObject(decodedJWT.getPayload());
                        userInfo = new JSONObject().put("header", jwtHeader).put("payload", jwtPayload);
                    } else if (containsValue(response.headers(), "application/x-www-form-urlencoded")
                            || containsValue(response.headers(), "text/plain")) {
                        // attempt to convert url encoded string to json
                        userInfo = queryToJson(body);
                    } else {
                        return CompletableFuture.failedFuture(
                                new AuthenticationException("Cannot handle Content-Type: "
                                        + response.headers().allValues("Content-Type")));
                    }

                    processNonStandardHeaders(userInfo, response, options.getScopeSeparator());
                    return CompletableFuture.completedFuture(userInfo);
                });
    }

    /**
     * Retrieve the public server JSON Web Key (JWK) required to verify the authenticity of issued ID and access tokens.
     */
    public CompletableFuture<JSONObject> jwkSet() {
        final JSONObject headers = new JSONObject();
        // specify preferred accepted content type, according to https://tools.ietf.org/html/rfc7517#section-8.5
        // there's a specific media type for this resource: application/jwk-set+json but we also allow plain application/json
        headers.put("Accept", "application/jwk-set+json, application/json");

        return fetch(HttpMethod.GET, options.getJwkPath(), headers, null)
                .thenCompose(response -> {
                    if (response.body() == null || response.body().length() == 0) {
                        return CompletableFuture.failedFuture(new RuntimeException("No Body"));
                    }

                    JSONObject json;
                    if (containsValue(response.headers(), "application/jwk-set+json") ||
                            containsValue(response.headers(), "application/json")) {
                        json = new JSONObject(response.body());
                    } else return CompletableFuture.failedFuture(
                            new RuntimeException("Cannot handle content type: "
                                    + response.headers().allValues("Content-Type")));

                    if (json.has("error")) {
                        return CompletableFuture.failedFuture(new RuntimeException(extractErrorDescription(json)));
                    } else {
                        // process the cache headers as recommended by: https://openid.net/specs/openid-connect-core-1_0.html#RotateEncKeys
                        List<String> cacheControl = response.headers().allValues(CACHE_CONTROL);
                        if (cacheControl != null) {
                            for (String header : cacheControl) {
                                // we need at least "max-age="
                                if (header.length() > 8) {
                                    Matcher match = MAX_AGE.matcher(header);
                                    if (match.find()) {
                                        try {
                                            json.put("maxAge", Long.valueOf(match.group(1)));
                                            break;
                                        } catch (RuntimeException e) {
                                            // ignore bad formed headers
                                        }
                                    }
                                }
                            }
                        }
                        return CompletableFuture.completedFuture(json);
                    }
                });
    }

    /**
     * The discovery will use the given site in the configuration options
     * and attempt to load the well known descriptor.
     *
     * @param config the initial options, it should contain the site url
     * @return an OAuth2 provider with the discovered option values
     */
    public CompletableFuture<OAuth2AuthenticationProvider> discover(final WebAPI webAPI, final OAuth2Options config) {
        if (config.getSite() == null) {
            CompletableFuture.failedFuture(new RuntimeException("the site url cannot be null"));
        }

        final String oidc_discovery_path = "/.well-known/openid-configuration";

        String issuer = config.getSite();
        if (issuer.endsWith(oidc_discovery_path)) {
            issuer = issuer.substring(0, issuer.length() - oidc_discovery_path.length());
        }

        // fetch the OpenID Connect provider metadata as defined in:
        // https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata
        return fetch(HttpMethod.GET, issuer + oidc_discovery_path,
                new JSONObject().put("Accept", "application/json"), null)
                .thenCompose(response -> {
                    if (response.statusCode() != 200) {
                        return CompletableFuture.failedFuture(
                                new RuntimeException("Bad Response [" + response.statusCode() + "] " + response.body()));
                    }

                    if (!containsValue(response.headers(), "application/json")) {
                        return CompletableFuture.failedFuture(
                                new RuntimeException("Cannot handle content type: "
                                        + response.headers().allValues("Content-Type")));
                    }

                    final JSONObject json = new JSONObject(response.body());

                    // some providers return errors as JSON
                    if (json.has("error")) {
                        return CompletableFuture.failedFuture(new RuntimeException(extractErrorDescription(json)));
                    }

                    // issuer validation
                    if (config.isValidateIssuer()) {
                        String issuerEndpoint = json.getString("issuer");
                        if (issuerEndpoint != null) {
                            // the provider is letting the user know the issuer endpoint, so we need to validate it
                            // by removing the trailing slash (if present) and comparing it to the received endpoint
                            if (issuerEndpoint.endsWith("/")) {
                                issuerEndpoint = issuerEndpoint.substring(0, issuerEndpoint.length() - 1);
                            }

                            if (!config.getSite().equals(issuerEndpoint)) {
                                return CompletableFuture.failedFuture(
                                        new RuntimeException("Issuer validation failed: received ["
                                                + issuerEndpoint + "]"));
                            }
                        }
                    }

                    config.setAuthorizationPath(json.optString("authorization_endpoint", null));
                    config.setTokenPath(json.optString("token_endpoint", null));
                    config.setLogoutPath(json.optString("end_session_endpoint", null));
                    config.setRevocationPath(json.optString("revocation_endpoint", null));
                    config.setUserInfoPath(json.optString("userinfo_endpoint", null));
                    config.setJwkPath(json.optString("jwks_uri", null));
                    config.setIntrospectionPath(json.optString("introspection_endpoint", null));

                    if (json.has("issuer")) {
                        // the discovery document includes the issuer, this means we can add it
                        JWTOptions jwtOptions = config.getJWTOptions();
                        if (jwtOptions == null) {
                            jwtOptions = new JWTOptions();
                            config.setJWTOptions(jwtOptions);
                        }

                        // set the issuer
                        jwtOptions.setIssuer(json.getString("issuer"));
                    }

                    // reset supported grant types
                    config.setSupportedGrantTypes(null);
                    if (json.has("grant_types_supported") && config.getFlow() != null) {
                        // optional config
                        JSONArray flows = json.getJSONArray("grant_types_supported");
                        flows.forEach(grantType -> config.addSupportedGrantType((String) grantType));

                        if (!flows.toList().contains(config.getFlow().getGrantType())) {
                            return CompletableFuture.failedFuture(new RuntimeException("Unsupported flow: " +
                                    config.getFlow().getGrantType() + ", allowed: " + flows));
                        }
                    }

                    return CompletableFuture.completedFuture(new OAuth2AuthenticationProvider(webAPI, config));
                });
    }

    /**
     * Logout the user from the OAuth2 provider.
     *
     * @param accessToken  the access token
     * @param refreshToken the refresh token
     */
    public CompletableFuture<Void> logout(String accessToken, String refreshToken) {
        final JSONObject headers = new JSONObject();
        headers.put("Authorization", "Bearer " + accessToken);

        final JSONObject form = new JSONObject();
        form.put("client_id", options.getClientId());
        if (options.getClientSecret() != null) {
            form.put("client_secret", options.getClientSecret());
        }
        if (refreshToken != null) {
            form.put("refresh_token", refreshToken);
        }

        headers.put("Content-Type", "application/x-www-form-urlencoded");
        final String payload = jsonToQuery(form);
        headers.put("Accept", "application/json,application/x-www-form-urlencoded;q=0.9");

        return fetch(HttpMethod.POST, options.getLogoutPath(), headers, payload)
                .thenCompose(response -> {
                    if (response.statusCode() != 200) {
                        return CompletableFuture.failedFuture(
                                new RuntimeException("Bad Response [" + response.statusCode() + "] " + response.body()));
                    }

                    return CompletableFuture.completedFuture(null);
                });
    }

    private CompletableFuture<HttpResponse<String>> fetch(HttpMethod method, String path,
                                                          JSONObject headers, String payload) {
        if (path == null || path.length() == 0) {
            // and this can happen as it is a config option that is dependent on the provider
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid path"));
        }

        final String url = path.charAt(0) == '/' ? options.getSite() + path : path;
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url));

        // apply the provider required headers
        JSONObject tmp = options.getHeaders();
        if (tmp != null) {
            for (Map.Entry<String, Object> kv : tmp.toMap().entrySet()) {
                requestBuilder.header(kv.getKey(), (String) kv.getValue());
            }
        }

        if (headers != null) {
            for (Map.Entry<String, Object> kv : headers.toMap().entrySet()) {
                requestBuilder.header(kv.getKey(), (String) kv.getValue());
            }
        }

        // specific UA
        if (options.getUserAgent() != null) {
            requestBuilder.header("User-Agent", options.getUserAgent());
        }

        if (method != HttpMethod.POST && method != HttpMethod.PATCH && method != HttpMethod.PUT) {
            payload = null;
        }

        // create a request
        return makeRequest(requestBuilder, payload);
    }

    private CompletableFuture<HttpResponse<String>> makeRequest(HttpRequest.Builder requestBuilder, String payload) {
        // send
        if (payload != null) {
            requestBuilder.POST(HttpRequest.BodyPublishers.ofByteArray(payload.getBytes()));
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    // read the body regardless
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        if (response.body() == null || response.body().length() == 0) {
                            return CompletableFuture.failedFuture(
                                    new RuntimeException("Status code: " + response.statusCode()));
                        } else {
                            if (containsValue(response.headers(), "application/json")) {
                                // if value is json, extract error, error_descriptions
                                JSONObject error = new JSONObject(response.body());
                                if (!error.optString("error").isEmpty()) {
                                    if (!error.optString("error_description").isEmpty()) {
                                        return CompletableFuture.failedFuture(
                                                new RuntimeException(error.getString("error") +
                                                        ": " + error.getString("error_description")));
                                    } else {
                                        return CompletableFuture.failedFuture(
                                                new RuntimeException(error.getString("error")));
                                    }
                                }
                            }
                            return CompletableFuture.failedFuture(
                                    new RuntimeException(response.statusCode() + ": " + response.body()));
                        }
                    } else {
                        return CompletableFuture.completedFuture(response);
                    }
                });
    }
}
