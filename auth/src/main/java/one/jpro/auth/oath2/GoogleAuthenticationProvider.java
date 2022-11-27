package one.jpro.auth.oath2;

import com.auth0.jwk.JwkException;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jpro.webapi.WebAPI;
import jakarta.annotation.Nonnull;
import jakarta.validation.constraints.NotBlank;
import one.jpro.auth.authentication.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Google authentication provider.
 *
 * @author Besmir Beqiri
 */
public class GoogleAuthenticationProvider implements AuthenticationProvider<OAuth2Credentials> {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthenticationProvider.class);

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";

    @Nonnull
    private final WebAPI webAPI;

    @Nonnull
    @NotBlank
    private final String clientId;

    @Nonnull
    @NotBlank
    private final String clientSecret;

    private final UrlJwkProvider urlJWKProvider;
    private final String redirectURL;

    public GoogleAuthenticationProvider(@Nonnull WebAPI webAPI,
                                        @Nonnull @NotBlank String clientId,
                                        @Nonnull @NotBlank String clientSecret) {
        this.webAPI = webAPI;
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        try {
            urlJWKProvider = new UrlJwkProvider(new URL(CERTS_URL));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }

        final var server = webAPI.getServer();
        redirectURL = (server.contains("localhost")) ?
                "http://" + server + "/" : "https://" + server + "/";
    }

    @Override
    public CompletableFuture<Authentication> authenticate(OAuth2Credentials credentials) {
        try {
            final var tokenJSon = generateToken();
            final var optionalEmail = verifyToken(tokenJSon.getString("id_token"));
            if (optionalEmail.isPresent()) {
                final var email = optionalEmail.get();
                final Map<String, Object> attributes = tokenJSon.toMap();
                return CompletableFuture.completedFuture(Authentication.build(email, attributes));
            } else {
                return CompletableFuture.failedFuture(
                        new AuthenticationException("Failed to connect via Google account."));
            }
        } catch (IOException | InterruptedException | JwkException ex) {
            return CompletableFuture.failedFuture(ex);
        }
    }

    public String loginUrl() {
        final var urlString = "https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?response_type=code&client_id="
                +clientId+"&scope=openid%20email&" +
                "redirect_uri=" + redirectURL +
                "&nonce=0394852-3190485-2490358";
        log.info("Login URL: " + urlString);
        return urlString;
    }

    private JSONObject generateToken() throws IOException, InterruptedException {
        var code = webAPI.getURLQueryParams().get("code");

        var body = "grant_type=authorization_code" + "&" +
                "code=" + code + "&" +
                "client_id=" + clientId + "&" +
                "client_secret=" + clientSecret + "&" +
                "redirect_uri=" + redirectURL;

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder().uri(URI.create(TOKEN_URL))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body.getBytes())).build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("Response: {}", response.body());

        return new JSONObject(response.body());
    }

    Optional<String> verifyToken(String token) throws JwkException {
        var decoded = JWT.decode(token);

        var jwk = urlJWKProvider.get(decoded.getKeyId());
        var algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
        var verifier = JWT.require(algorithm).build();
        var jwt = verifier.verify(token);
        return  (jwt.getExpiresAt().after(new Date())) ?
                Optional.ofNullable(decoded.getClaim("email").asString()) : Optional.empty();
    }
}
