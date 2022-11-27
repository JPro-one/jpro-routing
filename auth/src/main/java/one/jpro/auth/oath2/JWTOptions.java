package one.jpro.auth.oath2;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Options describing a JWT (Json Web Token).
 *
 * @author Besmir Beqiri
 */
public class JWTOptions {

    private static final JSONObject EMPTY = new JSONObject(Collections.emptyMap());

    private int leeway = 0;
    private boolean ignoreExpiration;
    private String algorithm = "HS256";
    private JSONObject header = EMPTY;
    private boolean noTimestamp;
    private int expires;
    private List<String> audience;
    private String issuer;
    private String subject;
    private List<String> permissions;
    private String nonceAlgorithm;

    /**
     * Default constructor.
     */
    public JWTOptions(){
    }

    public int getLeeway() {
        return leeway;
    }

    public void setLeeway(int leeway) {
        this.leeway = leeway;
    }

    public boolean isIgnoreExpiration() {
        return ignoreExpiration;
    }

    public void setIgnoreExpiration(boolean ignoreExpiration) {
        this.ignoreExpiration = ignoreExpiration;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public JSONObject getHeader() {
        return header;
    }

    public void setHeader(JSONObject header) {
        this.header = header;
    }

    public boolean isNoTimestamp() {
        return noTimestamp;
    }

    public void setNoTimestamp(boolean noTimestamp) {
        this.noTimestamp = noTimestamp;
    }

    public int getExpiresInSeconds() {
        return expires;
    }

    public JWTOptions setExpiresInSeconds(int expires) {
        this.expires = expires;
        return this;
    }

    public JWTOptions setExpiresInMinutes(int expiresInMinutes) {
        this.expires = expiresInMinutes * 60;
        return this;
    }

    public List<String> getAudience() {
        return audience;
    }

    public void setAudience(List<String> audience) {
        this.audience = audience;
    }

    public JWTOptions addAudience(String audience) {
        if (this.audience == null) {
            this.audience = new ArrayList<>();
        }
        this.audience.add(audience);
        return this;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public String getNonceAlgorithm() {
        return nonceAlgorithm;
    }

    public void setNonceAlgorithm(String nonceAlgorithm) {
        this.nonceAlgorithm = nonceAlgorithm;
    }
}
