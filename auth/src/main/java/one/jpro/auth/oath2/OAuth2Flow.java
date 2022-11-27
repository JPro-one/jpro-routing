package one.jpro.auth.oath2;

/**
 * OAuth2 flow.
 *
 * @author Besmir Beqiri
 */
public enum OAuth2Flow {

    AUTH_CODE("authorization_code"),
    PASSWORD("password");

    private final String grantType;

    OAuth2Flow(String grantType) {
        this.grantType = grantType;
    }

    public String getGrantType() {
        return grantType;
    }

    public static OAuth2Flow getFlow(String grantType) {
        for (var flow : values()) {
            if (flow.getGrantType().equals(grantType)) {
                return flow;
            }
        }
        return null;
    }
}
