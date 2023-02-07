package one.jpro.auth.oath2;

import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jpro.webapi.WebAPI;
import jakarta.validation.constraints.NotNull;
import one.jpro.auth.authentication.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * OAuth2 authentication provider.
 *
 * @author Besmir Beqiri
 */
public class OAuth2AuthenticationProvider implements AuthenticationProvider<Credentials> {

    private final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationProvider.class);

    @NotNull
    protected final WebAPI webAPI;
    @NotNull
    protected final OAuth2Options options;
    @NotNull
    protected final OAuth2API api;
    @NotNull
    private JwkProvider jwkProvider;

    public OAuth2AuthenticationProvider(WebAPI webAPI, OAuth2Options options) {
        this.webAPI = Objects.requireNonNull(webAPI, "WebAPI cannot be null");
        this.options = Objects.requireNonNull(options, "OAuth2 options cannot be null");
        this.api = new OAuth2API(options);

        try {
            jwkProvider = new UrlJwkProvider(new URL(options.getJwkPath()));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            jwkProvider = new JwkProviderBuilder(options.getJwkPath())
                    .cached(options.getJWTOptions().getCacheSize(), options.getJWTOptions().getExpiresIn())
                    .build();
        }
    }

    public String authorizeUrl(OAuth2Credentials credentials) {
        return api.authorizeURL(credentials.setNormalizedRedirectUri(normalizeUri(credentials.getRedirectUri())));
    }

    @Override
    public CompletableFuture<User> authenticate(Credentials credentials) {
        try {
            if (credentials instanceof UsernamePasswordCredentials) {
                UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;
                // validate
                usernamePasswordCredentials.validate(null);

                OAuth2Credentials oauth2Credentials = new OAuth2Credentials()
                        .setUsername(usernamePasswordCredentials.getUsername())
                        .setPassword(usernamePasswordCredentials.getPassword())
                        .setFlow(OAuth2Flow.PASSWORD);

                return authenticate(oauth2Credentials);
            }

            // if the credentials already contain a token, then validate it to confirm
            // that it can be reused, otherwise, based on the configured flow, request
            // a new token from the authority provider

            if (credentials instanceof TokenCredentials) {
                TokenCredentials tokenCredentials = (TokenCredentials) credentials;
                tokenCredentials.validate(null);

                // credentials already contain a token, validate it
                // attempt to create a user from the credentials
                try {
                    final User newUser = createUser(new JSONObject().put("access_token", tokenCredentials.getToken()));
                    // basic validation passed
                    return CompletableFuture.completedFuture(newUser);
                } catch (TokenExpiredException | IllegalStateException ex) {
                    log.error(ex.getMessage(), ex);
//                    return CompletableFuture.failedFuture(ex);
                } catch (JwkException ex) {
                    log.error(ex.getMessage(), ex);
//                    return CompletableFuture.failedFuture(new RuntimeException(ex.getMessage(), ex));
                }

                // the token is not JWT format or this authentication provider is not configured to use JWTs
                // in this case we must rely on token introspection in order to know more about its state
                // attempt to create a token object from the given string representation

                // not all providers support this, so we need to check if the call is possible
                if (options.getIntrospectionPath() == null) {
                    // this provider doesn't allow introspection, this means we are not able
                    // to perform any authentication
                    return CompletableFuture.failedFuture(
                            new RuntimeException("Can't authenticate `access_token`: " +
                                    "Provider doesn't support token introspection"));
                }

                // perform the introspection in accordance to RFC7662
                return api.tokenIntrospection("access_token", tokenCredentials.getToken())
                        .thenCompose(json -> {
                            // RFC7662 dictates that there is a boolean active field,
                            // however token info implementation may not return this
                            if (json.has("active") && json.getBoolean("active")) {
                                return CompletableFuture.failedFuture(new RuntimeException("Inactive Token"));
                            }

                            // validate client_id
                            if (json.has("client_id")) {
                                final String clientId = options.getClientId();
                                if (clientId != null && !clientId.equals(json.getString("client_id"))) {
                                    // client identifier for the OAuth2 client that requested this token
                                    log.info("Introspect `client_id` doesn't match configured `client_id`");
                                }
                            }

                            // attempt to create a user from the json object
                            try {
                                final User newUser = createUser(json);
                                // basic validation passed
                                return CompletableFuture.completedFuture(newUser);
                            } catch (TokenExpiredException | IllegalStateException ex) {
                                return CompletableFuture.failedFuture(ex);
                            } catch (JwkException ex) {
                                return CompletableFuture.failedFuture(new RuntimeException(ex.getMessage(), ex));
                            }
                        });
            }

            // from this point, the only allowed credentials subtype is OAuth2Credentials
            OAuth2Credentials oauth2Credentials = (OAuth2Credentials) credentials;

            final JSONObject params = new JSONObject();
            final OAuth2Flow flow;
            if (oauth2Credentials.getFlow() != null) {
                flow = oauth2Credentials.getFlow();
            } else {
                flow = options.getFlow();
            }

            // TODO: handle possible exceptions

            // Retrieve the authorization code
            oauth2Credentials.setCode(webAPI.getURLQueryParams().get("code"));
            if (oauth2Credentials.getCode() == null || oauth2Credentials.getCode().isBlank()) {
                return CompletableFuture.failedFuture(new RuntimeException("Authorization code is missing"));
            }

            // Validate credentials
            oauth2Credentials.validate(flow);

            if (options.getSupportedGrantTypes() != null && !options.getSupportedGrantTypes().isEmpty() &&
                    !options.getSupportedGrantTypes().contains(flow.getGrantType())) {
                return CompletableFuture.failedFuture(
                        new RuntimeException("Provided flow is not supported by provider"));
            }

            switch (flow) {
                case AUTH_CODE:
                    // code is always required. It's the code received on the web side
                    params.put("code", oauth2Credentials.getCode());
                    // must be identical to the redirect URI provided in the original link
                    if (oauth2Credentials.getRedirectUri() != null) {
                        params.put("redirect_uri", normalizeUri(oauth2Credentials.getRedirectUri()));
                    }
                    // the plaintext string that was previously hashed to create the code_challenge
                    if (oauth2Credentials.getCodeVerifier() != null) {
                        params.put("code_verifier", oauth2Credentials.getCodeVerifier());
                    }
                    break;

                case PASSWORD:
                    params.put("username", oauth2Credentials.getUsername())
                            .put("password", oauth2Credentials.getPassword());

                    if (oauth2Credentials.getScopes() != null) {
                        params.put("scope", String.join(options.getScopeSeparator(), oauth2Credentials.getScopes()));
                    }
                    break;

                case CLIENT:
                    // applications may need an access token to act on behalf of themselves rather than a user.
                    // in this case there are no parameters
                    if (oauth2Credentials.getScopes() != null) {
                        params.put("scope", String.join(options.getScopeSeparator(), oauth2Credentials.getScopes()));
                    }
                    break;

                default:
                    return CompletableFuture.failedFuture(
                            new RuntimeException("Current flow does not allow acquiring a token by the replay party"));
            }

            return api.token(flow.getGrantType(), params)
                    .thenCompose(json -> {
                        // attempt to create a user from the json object
                        try {
                            final User newUser = createUser(json, params);
                            oauth2Credentials.setUsername(newUser.getName());
                            // basic validation passed
                            return CompletableFuture.completedFuture(newUser);
                        } catch (TokenExpiredException | IllegalStateException ex) {
                            return CompletableFuture.failedFuture(ex);
                        } catch (JwkException ex) {
                            return CompletableFuture.failedFuture(new RuntimeException(ex.getMessage(), ex));
                        }
                    });
        } catch (ClassCastException | CredentialValidationException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    /**
     * Creates a OAuth2 authentication provider for OpenID Connect Discovery. The discovery will use the given
     * site in the configuration options and attempt to load the well-known descriptor.
     *
     * @return an {@link OAuth2AuthenticationProvider} instance.
     */
    public CompletableFuture<OAuth2AuthenticationProvider> discover() {
        return api.discover(webAPI, options);
    }

    public CompletableFuture<User> refresh(User user) {
        final Object refreshToken = user.getAttributes().get("refresh_token");
        if (refreshToken == null || refreshToken.toString().isBlank()) {
            return CompletableFuture.failedFuture(new RuntimeException("refresh_token is null or missing"));
        }

        return api.token("refresh_token", new JSONObject().put("refresh_token", refreshToken.toString()))
                .thenCompose(json -> {
                    // attempt to create a user from the json object
                    try {
                        final User newUser = createUser(json);
                        // basic validation passed
                        return CompletableFuture.completedFuture(newUser);
                    } catch (TokenExpiredException | IllegalStateException ex) {
                        return CompletableFuture.failedFuture(ex);
                    } catch (JwkException ex) {
                        return CompletableFuture.failedFuture(new RuntimeException(ex.getMessage(), ex));
                    }
                });
    }

    public CompletableFuture<Void> revoke(User user, String tokenType) {
        return api.tokenRevocation(tokenType, user.getAttributes().get(tokenType).toString());
    }

    /**
     * Retrieve user information and other attributes for a logged-in end-user.
     *
     * @param user the user (access token) to fetch the user information.
     * @return a {@link JSONObject} with the user information.
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#UserInfo">OpenID Connect Core 1.0</a>
     */
    public CompletableFuture<JSONObject> userInfo(User user) {
        Objects.requireNonNull(user, "User must not be null");
        final JSONObject userJSON = user.toJson();
        final JSONObject attributesJSON = userJSON.getJSONObject(User.KEY_ATTRIBUTES);

        return api.userInfo(attributesJSON.getString("access_token"))
                .thenCompose(json -> {
                    // validate if the subject of this token match the user subject
                    if (user.hasAttribute("sub")) {
                        final String userSub = attributesJSON.getJSONObject("jwt").getString("sub");
                        final JSONObject userInfoPayload = json.getJSONObject("payload");
                        if (!userSub.equals(userInfoPayload.getString("sub"))) {
                            return CompletableFuture.failedFuture(
                                    new AuthenticationException("User subject does not match UserInfo subject"));
                        }
                    }

                    // verify if expired
                    if (json.has("token")) {
                        try {
                            verifyToken(json.getString("token") ,false);
                        } catch (TokenExpiredException | IllegalStateException ex) {
                            return CompletableFuture.failedFuture(ex);
                        } catch (JwkException ex) {
                            return CompletableFuture.failedFuture(
                                    new AuthenticationException(ex.getMessage(), ex));
                        }
                    }

                    return CompletableFuture.completedFuture(json);
                });
    }

    private User createUser(@NotNull JSONObject json) throws JwkException, TokenExpiredException, IllegalStateException {
        return createUser(json, new JSONObject());
    }

    private User createUser(@NotNull JSONObject json, @NotNull JSONObject params) throws JwkException,
            TokenExpiredException, IllegalStateException {
        Objects.requireNonNull(json, "json can not be null");
        Objects.requireNonNull(params, "params can not be null");

        if (json.has("access_token")) {
            // attempt to create a user from the json object
            final String token = json.getString("access_token");

            // verify if the user is not expired
            // this may happen if the user tokens have been issued for future use for example
            final JSONObject jwtJSON = verifyToken(token, false);
            // Store JWT authorization
            params.put("jwt", jwtJSON);

            final String email = jwtJSON.getJSONObject("claims").getString("email");
            // Set principal name
            params.put(Authentication.KEY_NAME, email);
        }

        if (json.has("id_token")) {
            // attempt to create a user from the json object
            final String token = json.getString("id_token");

            // verify if the user is not expired
            // this may happen if the user tokens have been issued for future use for example
            final JSONObject jwtJSON = verifyToken(token, true);
            // Store JWT authorization
            params.put("jwt", jwtJSON);
        }

        JSONObject authJSON = new JSONObject(params, JSONObject.getNames(params));
        authJSON.put(Authentication.KEY_ATTRIBUTES, params);

        // TODO: Configure roles

        // Create authentication instance
        return Authentication.create(authJSON);
    }

    /**
     * Performs a token verification and basic validation.
     *
     * @param token the token string
     * @param idToken set to <code>true</code> if this token is an id_token, otherwise <code>false</code>
     * @return a {@link JSONObject} holding the Json Web Token information related to this token.
     * @throws JwkException if no jwk can be found using the given token kid
     * @throws TokenExpiredException if the token has expired
     * @throws IllegalStateException if the basic validation fails
     */
    private JSONObject verifyToken(String token, boolean idToken) throws JwkException,
            TokenExpiredException, IllegalStateException {
        final JWTOptions jwtOptions = options.getJWTOptions();

        JSONObject json;
        try {
            final DecodedJWT decodedToken = JWT.decode(token);
            final String alg = decodedToken.getAlgorithm();
            Algorithm algorithm = Algorithm.none();
            // TODO: Add support for other algorithms
            switch (alg) {
                case "HS256":
                    algorithm = Algorithm.HMAC256(options.getClientSecret());
                    break;
                case "RS256":
                    final Jwk jwk = jwkProvider.get(decodedToken.getKeyId());
                    algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                    break;
            }

            // Allow only secure algorithms
            if (Algorithm.none().equals(algorithm)) {
                throw new IllegalStateException("Algorithm \"none\" not allowed");
            }

            final JWTVerifier verifier = JWT.require(algorithm).build();
            final DecodedJWT verifiedToken = verifier.verify(token);
            json = jwtToJson(verifiedToken, idToken ? "id_token" : "access_token");
        } catch (com.auth0.jwt.exceptions.TokenExpiredException tex) {
            throw new TokenExpiredException(tex.getMessage(), tex.getExpiredOn());
        }

        // validate the audience
        if (json.has("aud")) {
            final JSONArray audience = json.getJSONArray("aud");
            if (audience == null || audience.isEmpty()) {
                throw new IllegalStateException("User audience is null or empty");
            }

            if (audience.length() > 0) {
                if (idToken || jwtOptions.getAudience() == null) {
                    // In reference to: https://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation
                    // The Client MUST validate that the aud (audience) Claim contains its client_id value registered at
                    // the Issuer identified by the iss (issuer) Claim as an audience. The aud (audience) Claim MAY contain
                    // an array with more than one element. The ID Token MUST be rejected if the ID Token does not list the
                    // Client as a valid audience, or if it contains additional audiences not trusted by the Client.
                    if (!audience.toString().contains(options.getClientId())) {
                        throw new IllegalStateException("Invalid JWT audience, expected: " + options.getClientId() +
                                ", actual: " + audience);
                    }
                } else {
                    final List<String> audList = audience.toList().stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    for (String aud : jwtOptions.getAudience()) {
                        if (!audList.contains(aud)) {
                            throw new IllegalStateException("Invalid JWT audience, expected: " + aud +
                                    ", actual: " + audience);
                        }
                    }
                }
            }
        }

        // validate the issuer
        if (jwtOptions.getIssuer() != null) {
            if (!jwtOptions.getIssuer().equals(json.getString("iss"))) {
                throw new IllegalStateException("Invalid JWT issuer, expected: " + jwtOptions.getIssuer() +
                        ", actual: " + json.getString("iss"));
            }
        }

        // validate authorised party
        if (idToken) {
            if (json.has("azp")) {
                if (!options.getClientId().equals(json.getString("azp"))) {
                    throw new IllegalStateException("Invalid authorised party, expected: " + options.getClientId() +
                            ", actual: " + json.getString("azp"));
                }

                final JSONArray audience = json.getJSONArray("aud");
                if (audience != null && audience.length() > 1) {
                    // In reference to: https://openid.net/specs/openid-connect-core-1_0.html#IDTokenValidation
                    // If the ID Token contains multiple audiences, the Client SHOULD verify that an azp Claim is present.
                    final List<String> audList = audience.toList().stream()
                            .map(Object::toString)
                            .collect(Collectors.toList());
                    if (audList.contains(json.getString("azp"))) {
                        throw new IllegalStateException("ID token with multiple audiences, " +
                                "doesn't contain the azp Claim value");
                    }
                }
            }
        }

        return json;
    }

    /**
     * Returns a JSON representation of a Json Web Token.
     *
     * @param jwt represents a Json Web Token that was decoded from its string representation
     * @param tokenType a string representation of the type of this token, like "access_token" or "id_token"
     * @return a {@link JSONObject} holding the JWT information.
     */
    private JSONObject jwtToJson(DecodedJWT jwt, String tokenType) {
        final JSONObject json = new JSONObject();
        // Decoded JWT info
        json.put(tokenType, jwt.getToken());
        Optional.ofNullable(jwt.getHeader()).ifPresent(header -> json.put("header", header));
        Optional.ofNullable(jwt.getPayload()).ifPresent(payload -> json.put("payload", payload));
        Optional.ofNullable(jwt.getSignature()).ifPresent(signature -> json.put("signature", signature));

        // Payload info
        Optional.ofNullable(jwt.getIssuer()).ifPresent(issuer -> json.put("iss", issuer));
        Optional.ofNullable(jwt.getSubject()).ifPresent(subject -> json.put("sub", subject));
        Optional.ofNullable(jwt.getAudience()).ifPresent(audience -> json.put("aud", new JSONArray(audience)));
        Optional.ofNullable(jwt.getExpiresAt()).map(Date::getTime).ifPresent(exp -> json.put("exp", exp));
        Optional.ofNullable(jwt.getIssuedAt()).map(Date::getTime).ifPresent(iat -> json.put("iat", iat));
        Optional.ofNullable(jwt.getNotBefore()).map(Date::getTime).ifPresent(nbr -> json.put("nbr", nbr));
        Optional.ofNullable(jwt.getId()).ifPresent(kid -> json.put("kid", kid));
        Optional.ofNullable(jwt.getClaim("azp")).ifPresent(azp -> json.put("azp", azp.asString()));
        Optional.ofNullable(jwt.getClaims()).ifPresent(claimMap -> json.put("claims", new JSONObject(claimMap)));
        return json;
    }

    private boolean hasExpired(User user) {
        if (user.getAttributes().containsKey("jwt")) {
            JSONObject jwtInfo = (JSONObject) user.getAttributes().get("jwt");
            if (jwtInfo.has("exp")) {
                final Instant expiredAt = Instant.ofEpochMilli(jwtInfo.getLong("exp"));
                return expiredAt.isBefore(Instant.now());
            }
        }
        return false;
    }

    private String normalizeUri(String uri) {
        // Complete uri if is partial
        String redirectUri = uri;
        if (redirectUri != null && redirectUri.charAt(0) == '/') {
            final String serverUrl = webAPI.getServer().contains("localhost") ?
                    "http://" + webAPI.getServer() : "https://" + webAPI.getServer();
            redirectUri = serverUrl + redirectUri;
        }
        return redirectUri;
    }
}
