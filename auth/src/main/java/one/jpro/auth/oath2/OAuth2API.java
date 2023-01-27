package one.jpro.auth.oath2;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.utils.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * OAuth2 API class.
 *
 * @author Besmir Beqiri
 */
public class OAuth2API {

    private static final Logger log = LoggerFactory.getLogger(OAuth2API.class);
    private static final Pattern MAX_AGE = Pattern.compile("max-age=\"?(\\d+)\"?");
    private static final String CACHE_CONTROL = "cache-control";
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    private final OAuth2Options options;
    private final HttpClient httpClient;

    public OAuth2API(OAuth2Options options) {
        this.options = options;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * The client sends the end-user's browser to this endpoint to request their authentication and consent.
     * This endpoint is used in the code and implicit OAuth 2.0 flows which require end-user interaction.
     * <p>
     * see: <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
     */
    public String authorizeURL(OAuth2Credentials credentials) {
        final JSONObject query = credentials.toJSON();

        final OAuth2Flow flow;
        if (query.has("flow") && !query.getString("flow").isBlank()) {
            flow = OAuth2Flow.getFlow(query.getString("flow"));
        } else {
            flow = options.getFlow();
        }

        if (flow != OAuth2Flow.AUTH_CODE) {
            throw new IllegalStateException("authorization URL cannot be computed for non AUTH_CODE flow");
        }

        if (query.has("scopes")) {
            // scopes have been passed as a list so the provider must generate the correct string for it
            query.put("scope", query.getJSONArray("scopes").join(options.getScopeSeparator())
                    .replace("\"", ""));
            query.remove("scopes");
        }

        query.put("response_type", "code");
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
     * <p>
     * see: <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
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
            for(String key : JSONObject.getNames(options.getExtraParams())){
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
                    if (containsValue(header,"application/json")) {
                        json = new JSONObject(response.body());
                    } else if (containsValue(header,"application/x-www-form-urlencoded")
                            || containsValue(header,"text/plain")) {
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
     * <p>
     * see: <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
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
     * <p>
     * see: <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
     */
    public CompletableFuture<Void> tokenRevocation(String tokenType, String token) {
        if (token == null) {
            return CompletableFuture.failedFuture(new RuntimeException("Cannot revoke null token"));
        }

        final JSONObject headers = new JSONObject();

        final boolean confidentialClient = options.getClientId() != null && options.getClientSecret() != null;

        if (confidentialClient) {
            String basic = options.getClientId() + ":" + options.getClientSecret();
            headers.put("Authorization", "Basic " + BASE64_ENCODER.encodeToString(basic.getBytes(StandardCharsets.UTF_8)));
        }

        final JSONObject form = new JSONObject();

        form.put("token", token).put("token_type_hint", tokenType);

        headers.put("Content-Type", "application/x-www-form-urlencoded");
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
     * @return the OAuth2 options with the discovered values
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

                    config.setAuthorizationPath(json.getString("authorization_endpoint"));
                    config.setTokenPath(json.getString("token_endpoint"));
                    config.setLogoutPath(json.getString("end_session_endpoint"));
                    config.setRevocationPath(json.getString("userinfo_endpoint"));
                    config.setUserInfoPath(json.getString("userinfo_endpoint"));
                    config.setJwkPath(json.getString("jwks_uri"));
                    config.setIntrospectionPath(json.getString("introspection_endpoint"));

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

    public CompletableFuture<HttpResponse<String>> fetch(HttpMethod method, String path,
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

    private String jsonToQuery(JSONObject json) {
        return json.toMap().entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private JSONObject queryToJson(String query) {
        if (query == null) {
            return null;
        }

        final JSONObject json = new JSONObject();
        final String[] pairs = query.split("&");
        for (String pair : pairs) {
            final int idx = pair.indexOf("=");
            final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
            final String value = (idx > 0 && pair.length() > idx + 1) ?
                    URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
            if (!json.has(key)) {
                json.put(key, value);
            } else {
                var oldValue = json.get(key);
                JSONArray array;
                if (oldValue instanceof JSONArray) {
                    array = (JSONArray) oldValue;
                } else {
                    array = new JSONArray();
                    array.put(oldValue);
                    json.put(key, array);
                }

                array.put(Objects.requireNonNullElse(value, JSONObject.NULL));
            }
        }

        return json;
    }

    private boolean containsValue(HttpHeaders headers, String value) {
        return headers.map().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .anyMatch(s -> s.contains(value));
    }

    private void processNonStandardHeaders(JSONObject json, HttpResponse<String> response, String scopeSeparator) {
        // inspect the response header for the non-standard:
        // X-OAuth-Scopes and X-Accepted-OAuth-Scopes
        final var xOAuthScopes = response.headers().firstValue("X-OAuth-Scopes");
        final var xAcceptedOAuthScopes = response.headers().firstValue("X-OAuth-Scopes");

        xOAuthScopes.ifPresent(scopes -> {
            log.trace("Received non-standard X-OAuth-Scopes: {}", scopes);
            if (json.has("scope")) {
                json.put("scope", json.getString("scope") + scopeSeparator + scopes);
            } else {
                json.put("scope", scopes);
            }
        });

        xAcceptedOAuthScopes.ifPresent(scopes -> {
            log.trace("Received non-standard X-OAuth-Scopes: {}", scopes);
            json.put("acceptedScopes", scopes);
        });
    }

    private String extractErrorDescription(JSONObject json) {
        if (json == null) {
            return "null";
        }

        String description;
        var error = json.get("error");
        if (error instanceof JSONObject) {
            description = ((JSONObject) error).getString("message");
        } else {
            description = json.optString("error_description", json.getString("error"));
        }

        if (description == null) {
            return "null";
        }

        return description;
    }
}
