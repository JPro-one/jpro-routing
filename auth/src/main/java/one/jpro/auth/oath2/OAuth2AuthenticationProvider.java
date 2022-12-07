package one.jpro.auth.oath2;

import com.auth0.jwk.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.jpro.webapi.WebAPI;
import one.jpro.auth.authentication.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * OAuth2 authentication provider.
 *
 * @author Besmir Beqiri
 */
public class OAuth2AuthenticationProvider implements AuthenticationProvider<Credentials> {

    private final WebAPI webAPI;
    private final OAuth2Options options;
    private final OAuth2API api;

    private JwkProvider jwkProvider;

    public OAuth2AuthenticationProvider(WebAPI webAPI, OAuth2Options options) {
        this.webAPI = webAPI;
        this.options = options;
        this.api = new OAuth2API(options);

        try {
            jwkProvider = new UrlJwkProvider(new URL(options.getJwkPath()));
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            jwkProvider = new JwkProviderBuilder(options.getJwkPath())
                .cached(options.getJwtOptions().getCacheSize(), options.getJwtOptions().getExpiresIn())
                .build();
        }
    }

    public String authorizeUrl(OAuth2Credentials credentials) {
        return api.authorizeURL(credentials.normalizedRedirectUri(normalizeUri(credentials.getRedirectUri())));
    }

    @Override
    public CompletableFuture<User> authenticate(Credentials credentials) {
        try {
            if (credentials instanceof UsernamePasswordCredentials) {
                UsernamePasswordCredentials usernamePasswordCredentials = (UsernamePasswordCredentials) credentials;
                // validate
                usernamePasswordCredentials.validate(null);

                OAuth2Credentials oauth2Credentials = new OAuth2Credentials()
                        .username(usernamePasswordCredentials.getUsername())
                        .password(usernamePasswordCredentials.getPassword())
                        .flow(OAuth2Flow.PASSWORD);

                return authenticate(oauth2Credentials);
            }

            OAuth2Credentials oauth2Credentials = (OAuth2Credentials) credentials;

            final JSONObject params = new JSONObject();
            final OAuth2Flow flow;
            if (oauth2Credentials.getFlow() != null) {
                flow = oauth2Credentials.getFlow();
            } else {
                flow = options.getFlow();
            }

            // Retrieve the authorization code
            oauth2Credentials.code(webAPI.getURLQueryParams().get("code"));
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
                    .thenCompose(tokenJSON -> {
                        // attempt to create an user from the json object
                        User user = null;

                        if (tokenJSON.has("access_token")) {
                            try {
                                // attempt to decode tokens if jwt keys are available
                                final String id_token = tokenJSON.getString("id_token");
                                final DecodedJWT decodedToken = JWT.decode(id_token);
                                final Jwk jwk = jwkProvider.get(decodedToken.getKeyId());

                                // final step, verify if the user is not expired
                                // this may happen if the user tokens have been issued for future use for example
                                final Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                                final JWTVerifier verifier = JWT.require(algorithm).build();
                                final DecodedJWT verifiedToken = verifier.verify(id_token);
                                if (verifiedToken.getExpiresAt().toInstant().isAfter(Instant.now())) {
                                    final String name = decodedToken.getClaim("email").asString();
                                    oauth2Credentials.username(name);
                                    // Set principal name
                                    params.put(Authentication.KEY_NAME, name);
                                    // Store JWT authorization
                                    params.put("jwt", jwtToJson(decodedToken,"access_token"));

                                    // TODO: Configure roles

                                    JSONObject authJSON = new JSONObject(params, JSONObject.getNames(params));
                                    authJSON.put(Authentication.KEY_ATTRIBUTES, params);

                                    // Create authorization instance
                                    user = Authentication.create(authJSON);
                                } else {
                                    return CompletableFuture.failedFuture(new RuntimeException("Authorization is expired"));
                                }
                            } catch (JwkException ex) {
                                return CompletableFuture.failedFuture(new RuntimeException(ex.getMessage(), ex));
                            }
                        }

                        // TODO: handle "id_token"

                        // basic validation passed, the token is not expired
                        return CompletableFuture.completedFuture(user);
                    });

        } catch (ClassCastException | CredentialValidationException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    private JSONObject jwtToJson(DecodedJWT jwt, String tokenKey) {
        final JSONObject json = new JSONObject();
        // Decoded JWT info
        json.put(tokenKey, jwt.getToken());
        Optional.ofNullable(jwt.getHeader()).ifPresent(header -> json.put("header", header));
        Optional.ofNullable(jwt.getPayload()).ifPresent(payload -> json.put("payload", payload));
        Optional.ofNullable(jwt.getSignature()).ifPresent(signature -> json.put("signature", signature));

        // Payload info
        Optional.ofNullable(jwt.getIssuer()).ifPresent(issuer -> json.put("issuer", issuer));
        Optional.ofNullable(jwt.getSubject()).ifPresent(subject -> json.put("subject", subject));
        Optional.ofNullable(jwt.getAudience()).ifPresent(audience -> json.put("audience", audience));
        Optional.ofNullable(jwt.getExpiresAt()).map(Date::getTime).ifPresent(exp -> json.put("exp", exp));
        Optional.ofNullable(jwt.getIssuedAt()).map(Date::getTime).ifPresent(iat -> json.put("iat", iat));
        Optional.ofNullable(jwt.getNotBefore()).map(Date::getTime).ifPresent(nbr -> json.put("nbr", nbr));
        Optional.ofNullable(jwt.getId()).ifPresent(kid -> json.put("kid", kid));
        Optional.ofNullable(jwt.getClaims()).ifPresent(claimMap -> {
            JSONArray jsonArray = new JSONArray(claimMap.entrySet());
            json.put("claims", jsonArray);
        });
        return json;
    }

    private String normalizeUri(String uri) {
        // Complete uri if is partial
        var redirectUri = uri;
        if (redirectUri != null && redirectUri.charAt(0) == '/') {
            final var serverUrl = webAPI.getServer().contains("localhost") ?
                    "http://" + webAPI.getServer()  : "https://" + webAPI.getServer();
            redirectUri = serverUrl + redirectUri;
        }
        return redirectUri;
    }
}
