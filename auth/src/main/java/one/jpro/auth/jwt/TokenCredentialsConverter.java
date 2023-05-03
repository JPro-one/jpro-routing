package one.jpro.auth.jwt;

import org.json.JSONObject;

/**
 * Converter for {@link TokenCredentials} class.
 *
 * @author Besmir Beqiri
 */
public interface TokenCredentialsConverter {

    /**
     * Creates a token credentials object with the given JSON formatted string.
     *
     * @param json the string representation of the JSON object holding the token credential's information.
     */
    static TokenCredentials fromJSON(String json) {
        return new TokenCredentials(new JSONObject(json));
    }

    /**
     * Convert the token credential information to JSON format and provides it as a string.
     *
     * @return a string in JSON format.
     */
    static String toJSON(TokenCredentials tokenCredentials) {
        return tokenCredentials.toJSON().toString();
    }
}
