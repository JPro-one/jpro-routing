package one.jpro.auth.http;

import com.jpro.webapi.WebAPI;
import javafx.stage.Stage;
import one.jpro.auth.http.impl.AuthenticationServerImpl;
import one.jpro.auth.http.impl.JProServerImpl;
import one.jpro.auth.utils.AuthUtils;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.*;

/**
 * Authorization server interface.
 *
 * @author Besmir Beqiri
 */
public interface AuthenticationServer extends AutoCloseable {

    /**
     * Creates a local authorization server. This method must be used
     * only for desktop/mobile applications that run locally.
     *
     * @return the auth server
     * @throws AuthenticationServerException if an error occurs
     */
    static AuthenticationServer create() {
        return create(null);
    }

    /**
     * Creates an authorization server. If the application is running
     * in a browser via JPro server, then a wrapper over JPro is returned.
     * If the application is not running inside the browser,
     * then a local authorization server is created and returned.
     *
     * @param stage the application stage
     * @return the auth server
     * @throws AuthenticationServerException if an error occurs
     */
    static AuthenticationServer create(Stage stage) throws AuthenticationServerException {
        if (WebAPI.isBrowser() && stage != null) {
            WebAPI webAPI = WebAPI.getWebAPI(stage);
            return new JProServerImpl(webAPI);
        }

        return AuthenticationServerImpl.getInstance(stage);
    }

    /**
     * Starts the server. If the application is running in a
     * browser via JPro server, then this method does nothing.
     */
    void start();

    /**
     * Stops the server. If the application is running in a
     * browser via JPro server, then this method does nothing.
     */
    void stop();

    @Override
    default void close() {
        stop();
    }

    /**
     * Return the server host.
     *
     * @return the server host
     */
    String getServerHost();

    /**
     * Return the server port.
     *
     * @return the server port
     */
    int getServerPort();

    default Map<String, String> getQueryParams() {
        Map<String, String> result = new HashMap<>();
        final Map<String, List<String>> params = getParameters();
        for (String key : params.keySet()) {
            result.put(key, params.get(key).get(0));
        }

        return result;
    }

    /**
     * Return the parameters of the request.
     *
     * @return the parameters
     */
    default Map<String, List<String>> getParameters() {
        final String uri = getFullRequestURL();
        int qmi = uri.indexOf('?');
        return (qmi >= 0) ? decodeParams(uri.substring(qmi + 1)) : Collections.emptyMap();
    }

    /**
     * Decodes parameters in percent-encoded URI-format (e.g.
     * "name=Jack%20Daniels&pass=Single%20Malt") and adds them to given Map.
     *
     * @param queryParams the parameters
     */
    default Map<String, List<String>> decodeParams(String queryParams) {
        AuthUtils.requireNonNullOrBlank(queryParams, "queryParams cannot be null or blank");

        final Map<String, List<String>> params = new HashMap<>();

        StringTokenizer st = new StringTokenizer(queryParams, "&");
        while (st.hasMoreTokens()) {
            String e = st.nextToken();
            int sep = e.indexOf('=');
            String key;
            String value;

            if (sep >= 0) {
                key = AuthUtils.decodePercent(e.substring(0, sep)).trim();
                value = AuthUtils.decodePercent(e.substring(sep + 1));
            } else {
                key = AuthUtils.decodePercent(e).trim();
                value = "";
            }

            List<String> values = params.computeIfAbsent(key, k -> new ArrayList<>());
            values.add(value);
        }

        return params;
    }

    /**
     * Return the full URL (with query string) the client used to request the server.
     *
     * @return the URL
     */
    String getFullRequestURL();

    /**
     * Return the full URL (without the query string) the client used to request the server.
     *
     * @return the URL
     */
    default String getRequestURL() {
        var idx = getFullRequestURL().indexOf('?');
        if (idx != -1) {
            return getFullRequestURL().substring(0, idx);
        }
        return getFullRequestURL();
    }

    /**
     * Opens the given URI in the browser.
     *
     * @param uri the URI to open
     */
    void openURL(@NotNull URI uri);
}
