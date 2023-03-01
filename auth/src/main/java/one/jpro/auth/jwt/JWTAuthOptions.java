package one.jpro.auth.jwt;

/**
 * Options describing how an {@link JWTAuthenticationProvider} should behave.
 *
 * @author Besmir Beqiri
 */
public class JWTAuthOptions {

    private static final JWTOptions JWT_OPTIONS = new JWTOptions();

    private String site;
    private JWTOptions jwtOptions;

    /**
     * Default constructor.
     */
    public JWTAuthOptions() {
        this.jwtOptions = JWT_OPTIONS;
    }

    /**
     * Copy constructor.
     *
     * @param other the options to copy
     */
    public JWTAuthOptions(JWTAuthOptions other) {
        this.site = other.site;
        this.jwtOptions = other.jwtOptions;
    }

    public String getSite() {
        return site;
    }

    public JWTAuthOptions setSite(String site) {
        this.site = site;
        return this;
    }

    public JWTOptions getJWTOptions() {
        return jwtOptions;
    }

    public JWTAuthOptions setJWTOptions(JWTOptions jwtOptions) {
        this.jwtOptions = jwtOptions;
        return this;
    }
}
