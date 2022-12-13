package one.jpro.auth.utils;

/**
 * Utility methods.
 *
 * @author Besmir Beqiri
 */
public final class Utils {

    /**
     * Checks that the specified string is not {@code null} and
     * throws a customized {@link NullPointerException} if it is, or blank
     * and throws a customized {@link IllegalArgumentException} if it is.
     * This method is designed primarily for doing parameter validation in methods and
     * constructors with multiple parameters, as demonstrated below:
     * <blockquote><pre>
     * public Credentials(String username, String password) {
     *     this.username = Utils.requireNonNullOrBlank(username, "id must not be null or blank");
     *     this.password = Utils.requireNonNullOrBlank(password, "password must not be null or blank");
     * }
     * </pre></blockquote>
     *
     * @param str     the string to check for nullity
     * @param message detail message to be used in the event that a {@code
     *                NullPointerException} is thrown
     * @return {@code str} if not {@code null} or not blank
     * @throws NullPointerException if {@code str} is {@code null}
     * @throws IllegalArgumentException if {@code str} is blank
     */
    public static String requireNonNullOrBlank(String str, String message) {
        if (str == null)
            throw new NullPointerException(message);
        if (str.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return str;
    }

    private Utils() {
        // Hide the default constructor.
    }
}
