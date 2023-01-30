package one.jpro.auth.oath2.provider;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.authentication.AuthenticationProvider;
import one.jpro.auth.oath2.OAuth2AuthenticationProvider;
import one.jpro.auth.oath2.OAuth2Flow;
import one.jpro.auth.oath2.OAuth2Options;
import org.json.JSONObject;

import java.util.concurrent.CompletableFuture;

/**
 * Simplified factory to create an {@link AuthenticationProvider} for Google.
 *
 * @author Besmir Beqiri
 */
public class GoogleAuthenticationProvider extends OAuth2AuthenticationProvider {

    /**
     * Create an {@link OAuth2AuthenticationProvider} for Google.
     *
     * @param webAPI  the JPro WebAPI
     * @param options custom OAuth2 options
     */
    public GoogleAuthenticationProvider(final WebAPI webAPI, final OAuth2Options options) {
        super(webAPI, options);
    }

    /**
     * Create an {@link OAuth2AuthenticationProvider} for Google.
     *
     * @param webAPI       the JPro WebAPI
     * @param clientId     the client id given to you by Google
     * @param clientSecret the client secret given to you by Google
     */
    public GoogleAuthenticationProvider(final WebAPI webAPI, final String clientId, final String clientSecret) {
        super(webAPI, new OAuth2Options()
                .setFlow(OAuth2Flow.AUTH_CODE)
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setSite("https://accounts.google.com")
                .setTokenPath("https://accounts.google.com/o/oauth2/token")
                .setAuthorizationPath("https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount")
                .setIntrospectionPath("https://accounts.google.com/o/oauth2/tokeninfo")
                .setUserInfoPath("https://www.googleapis.com/oauth2/v1/userinfo")
                .setJwkPath("https://www.googleapis.com/oauth2/v3/certs")
                .setRevocationPath("https://oauth2.googleapis.com/revoke"));
    }

    /**
     * Create an {@link OAuth2AuthenticationProvider} for OpenID Connect Discovery. The discovery will use the default
     * site in the configuration options and attempt to load the well-known descriptor. If a site is provided, then
     * it will be used to do the lookup.
     *
     * @param webAPI  the JPro WebAPI
     * @param options custom OAuth2 options
     * @return a future with the instantiated {@link OAuth2AuthenticationProvider}
     */
    public static CompletableFuture<OAuth2AuthenticationProvider> discover(final WebAPI webAPI,
                                                                           final OAuth2Options options) {
        final String site = options.getSite() == null ? "https://accounts.google.com" : options.getSite();

        return new GoogleAuthenticationProvider(webAPI,
                new OAuth2Options(options)
                        .setSite(site)
                        .setUserInfoParams(new JSONObject().put("alt", "json")))
                        .discover();
    }
}
