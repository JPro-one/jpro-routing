package one.jpro.auth.oath2.provider;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.authentication.AuthenticationProvider;
import one.jpro.auth.oath2.OAuth2AuthenticationProvider;
import one.jpro.auth.oath2.OAuth2Flow;
import one.jpro.auth.oath2.OAuth2Options;

/**
 * Simplified factory to create an {@link AuthenticationProvider} for Google.
 *
 * @author Besmir Beqiri
 */
public class GoogleAuthenticationProvider extends OAuth2AuthenticationProvider {

    /**
     * Create an {@link OAuth2AuthenticationProvider} for Google.
     *
     * @param options custom OAuth2 options
     */
    public GoogleAuthenticationProvider(WebAPI webAPI, OAuth2Options options) {
        super(webAPI, options);
    }

    /**
     * Create an {@link OAuth2AuthenticationProvider} for Google.
     *
     * @param clientId          the client id given to you by Google
     * @param clientSecret      the client secret given to you by Google
     */
    public GoogleAuthenticationProvider(WebAPI webAPI, String clientId, String clientSecret) {
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
}
