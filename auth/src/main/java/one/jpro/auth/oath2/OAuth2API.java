package one.jpro.auth.oath2;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.utils.HttpMethod;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
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

    private final WebAPI webAPI;
    private final OAuth2Options options;
    private final HttpClient httpClient;

    public OAuth2API(WebAPI webAPI, OAuth2Options options) {
        this.webAPI = webAPI;
        this.options = options;

        httpClient = HttpClient.newHttpClient();
    }

    /**
     * The client sends the end-user's browser to this endpoint to request their authentication and consent.
     * This endpoint is used in the code and implicit OAuth 2.0 flows which require end-user interaction.
     * <p>
     * see: <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
     */
    public String authorizeURL(JSONObject params) {
        final JSONObject query = new JSONObject(params);

        final OAuth2Flow flow;
        if (params.getString("flow") != null && !params.getString("flow").isEmpty()) {
            flow = OAuth2Flow.getFlow(params.getString("flow"));
        } else {
            flow = options.getFlow();
        }

        if (flow != OAuth2Flow.AUTH_CODE) {
            throw new IllegalStateException("authorization URL cannot be computed for non AUTH_CODE flow");
        }

        if (query.has("scopes")) {
            // scopes have been passed as a list so the provider must generate the correct string for it
            final var scopes = query.getJSONArray("scopes").toList()
                    .stream().map(Object::toString).collect(Collectors.toList());
            query.put("scope", String.join(options.getScopeSeparator(), scopes));
            query.remove("scopes");
        }

        query.put("response_type", "code");
        String clientId = options.getClientId();
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

        final String path = options.getAuthorizationPath();
        final String url = path.charAt(0) == '/' ? options.getSite() + path : path;

        return url + '?' + jsonToQuery(query);
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
        final JSONObject form = new JSONObject(params, JSONObject.getNames(params));
        if (options.getExtraParams() != null) {
            for(String key : JSONObject.getNames(options.getExtraParams())){
                form.put(key, options.getExtraParams().get(key));
            }
        }

        form.put("grant_type", grantType);

        if (!confidentialClient) {
            String clientId = options.getClientId();
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
        final ByteBuffer payload = jsonToQuery(form);

        // specify preferred accepted content type
        headers.put("Accept", "application/json,application/x-www-form-urlencoded;q=0.9");

        return fetch(HttpMethod.POST, options.getTokenPath(), headers, payload)
                .thenCompose(response -> {
                    if (response.body() == null || response.body().length() == 0) {
                        return CompletableFuture.failedFuture(new RuntimeException("No Body"));
                    }

                    JSONObject json;
                    final var headerMap = response.headers().map();
                    if (headerMap.containsValue("application/json")) {
                        try {
                            json = new JSONObject(response.body());
                        } catch (RuntimeException ex) {
                            return CompletableFuture.failedFuture(ex);
                        }
                    } else if (headerMap.containsValue("application/x-www-form-urlencoded")
                            || headerMap.containsValue("text/plain")) {
                        try {
                            json = queryToJson(response.body());
                        } catch (RuntimeException ex) {
                            return CompletableFuture.failedFuture(ex);
                        }
                    } else {
                        return CompletableFuture.failedFuture(
                                new RuntimeException("Cannot handle content type: " + headerMap.get("Content-Type")));
                    }

                    try {
                        if (json == null || json.has("error")) {
                            return CompletableFuture.failedFuture(
                                    new RuntimeException(extractErrorDescription(json)));
                        } else {
                            processNonStandardHeaders(json, response, options.getScopeSeparator());
                            return CompletableFuture.completedFuture(json);
                        }
                    } catch (RuntimeException ex) {
                        return CompletableFuture.failedFuture(ex);
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
        final ByteBuffer payload = jsonToQuery(form);
        // specify preferred accepted accessToken type
        headers.put("Accept", "application/json,application/x-www-form-urlencoded;q=0.9");

        return fetch(HttpMethod.POST, options.getIntrospectionPath(), headers, payload)
                .thenCompose(response -> {
                    if (response.body() == null || response.body().length() == 0) {
                        return CompletableFuture.failedFuture(new RuntimeException("No Body"));
                    }

                    JSONObject json;

                    final var headerMap = response.headers().map();
                    if (headerMap.containsValue("application/json")) {
                        try {
                            json = new JSONObject(response.body());
                        } catch (RuntimeException e) {
                            return CompletableFuture.failedFuture(e);
                        }
                    } else if (headerMap.containsValue("application/x-www-form-urlencoded") ||
                            headerMap.containsValue("text/plain")) {
                        try {
                            json = queryToJson(response.body());
                        } catch (RuntimeException ex) {
                            return CompletableFuture.failedFuture(ex);
                        }
                    } else {
                        return CompletableFuture.failedFuture(
                                new RuntimeException("Cannot handle accessToken type: "
                                        + headerMap.get("Content-Type")));
                    }

                    try {
                        if (json == null || json.has("error")) {
                            return CompletableFuture.failedFuture(
                                    new RuntimeException(extractErrorDescription(json)));
                        } else {
                            processNonStandardHeaders(json, response, options.getScopeSeparator());
                            return CompletableFuture.completedFuture(json);
                        }
                    } catch (RuntimeException e) {
                        return CompletableFuture.failedFuture(e);
                    }
                });
    }

    /**
     * Revoke an obtained access or refresh token.
     * <p>
     * see: <a href="https://tools.ietf.org/html/rfc6749">https://tools.ietf.org/html/rfc6749</a>
     */
    public Future<Void> tokenRevocation(String tokenType, String token) {
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
        final ByteBuffer payload = jsonToQuery(form);
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
    public Future<JSONObject> jwkSet() {
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
                    final var headerMap = response.headers().map();
                    if (headerMap.containsValue("application/jwk-set+json") ||
                            headerMap.containsValue("application/json")) {
                        try {
                            json = new JSONObject(response.body());
                        } catch (RuntimeException ex) {
                            return CompletableFuture.failedFuture(ex);
                        }
                    } else {
                        return CompletableFuture.failedFuture(
                                new RuntimeException("Cannot handle content type: " + headerMap.get("Content-Type")));
                    }

                    try {
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
                    } catch (RuntimeException ex) {
                        return CompletableFuture.failedFuture(ex);
                    }
                });
    }

    public CompletableFuture<HttpResponse<String>> fetch(HttpMethod method, String path,
                                                         JSONObject headers, ByteBuffer payload) {

        if (path == null || path.length() == 0) {
            // and this can happen as it is a config option that is dependent on the provider
            return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid path"));
        }

        final String url = path.charAt(0) == '/' ? options.getSite() + path : path;
        log.debug("Fetching URL: " + url);

        HttpRequest.Builder httpRequestBuilder = HttpRequest.newBuilder();

        // apply the provider required headers
        JSONObject tmp = options.getHeaders();
        if (tmp != null) {
            for (Map.Entry<String, Object> kv : tmp.toMap().entrySet()) {
                httpRequestBuilder.header(kv.getKey(), (String) kv.getValue());
            }
        }

        if (headers != null) {
            for (Map.Entry<String, Object> kv : headers.toMap().entrySet()) {
                httpRequestBuilder.header(kv.getKey(), (String) kv.getValue());
            }
        }

        // specific UA
        if (options.getUserAgent() != null) {
            httpRequestBuilder.header("User-Agent", options.getUserAgent());
        }

        if (method != HttpMethod.POST && method != HttpMethod.PATCH && method != HttpMethod.PUT) {
            payload = null;
        }

        // create a request
        return makeRequest(httpRequestBuilder, payload);
    }

    private CompletableFuture<HttpResponse<String>> makeRequest(HttpRequest.Builder requestBuilder, ByteBuffer payload) {
        // send
        if (payload != null) {
            requestBuilder.PUT(HttpRequest.BodyPublishers.ofByteArray(payload.array()));
        }

        return httpClient.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString())
                .thenCompose(oauth2Response -> {
                    // read the body regardless
                    if (oauth2Response.statusCode() < 200 || oauth2Response.statusCode() >= 300) {
                        if (oauth2Response.body() == null || oauth2Response.body().length() == 0) {
                            return CompletableFuture.failedFuture(
                                    new RuntimeException("Status code: " + oauth2Response.statusCode()));
                        } else {
                            if (oauth2Response.headers().map().containsValue("application/json")) {
                                // if value is json, extract error, error_descriptions
                                try {
                                    JSONObject error = new JSONObject(oauth2Response.body());
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
                                } catch (RuntimeException ignore) {
                                }
                            }
                            return CompletableFuture.failedFuture(
                                    new RuntimeException(oauth2Response.statusCode() + ": " + oauth2Response.body()));
                        }
                    } else {
                        return CompletableFuture.completedFuture(oauth2Response);
                    }
                });
    }

    private ByteBuffer jsonToQuery(JSONObject json) {
        final var sb = new StringBuilder();

        for (Map.Entry<String, ?> entry : json.toMap().entrySet()) {
            sb.append('&');
            sb.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            sb.append('=');
            var value = entry.getValue();
            if (value != null) {
                sb.append(URLEncoder.encode(value.toString(), StandardCharsets.UTF_8));
            }
        }

        return ByteBuffer.wrap(sb.toString().getBytes());
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
