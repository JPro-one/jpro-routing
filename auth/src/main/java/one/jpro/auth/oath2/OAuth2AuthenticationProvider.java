package one.jpro.auth.oath2;

import com.jpro.webapi.WebAPI;
import one.jpro.auth.authentication.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * OAuth2 authentication provider.
 *
 * @author Besmir Beqiri
 */
public class OAuth2AuthenticationProvider implements AuthenticationProvider<OAuth2Credentials> {

    private static final Logger log = LoggerFactory.getLogger(OAuth2AuthenticationProvider.class);

    private final WebAPI webAPI;
    private final OAuth2Options options;

    public OAuth2AuthenticationProvider(WebAPI webAPI, OAuth2Options options) {
        this.webAPI = webAPI;
        this.options = options;
    }

    @Override
    public CompletableFuture<Authentication> authenticate(OAuth2Credentials credentials) {
        return CompletableFuture.failedFuture(new AuthenticationException("To be implemented"));
    }
}
