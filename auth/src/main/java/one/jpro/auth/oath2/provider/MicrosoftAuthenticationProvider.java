package one.jpro.auth.oath2.provider;

import one.jpro.auth.authentication.AuthenticationProvider;
import one.jpro.auth.oath2.JWTOptions;
import one.jpro.auth.oath2.OAuth2AuthenticationProvider;
import one.jpro.auth.oath2.OAuth2Flow;
import one.jpro.auth.oath2.OAuth2Options;

/**
 * Simplified factory to create an {@link AuthenticationProvider} for Microsoft.
 *
 * @author Besmir Beqiri
 */
public class MicrosoftAuthenticationProvider extends OAuth2AuthenticationProvider {

    /**
     * Create an {@link OAuth2AuthenticationProvider} for Microsoft.
     *
     * @param options custom OAuth2 options
     */
    public MicrosoftAuthenticationProvider(OAuth2Options options) {
        super(options);
    }

    /**
     * Create an {@link OAuth2AuthenticationProvider} for Microsoft.
     *
     * @param clientId          the client id given to you by Google
     * @param clientSecret      the client secret given to you by Google
     * @param tenant            the guid of your application
     */
    public MicrosoftAuthenticationProvider(String clientId, String clientSecret, String tenant) {
        super(new OAuth2Options()
                .setFlow(OAuth2Flow.AUTH_CODE)
                .setClientId(clientId)
                .setClientSecret(clientSecret)
//                .setTenant(tenant)
                .setSite("https://login.microsoftonline.com/" + tenant)
                .setTokenPath("/oauth2/v2.0/token")
                .setAuthorizationPath("/oauth2/v2.0/authorize")
                .setJwkPath("https://login.microsoftonline.com/" + tenant + "/discovery/v2.0/keys")
                .setJwtOptions(new JWTOptions()
                        .setNonceAlgorithm("SHA-256")));
    }
}
