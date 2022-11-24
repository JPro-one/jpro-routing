package one.jpro.auth.authentication;

/**
 * Abstract representation of a Credentials object.
 *
 * @author Besmir Beqiri
 */
public interface Credentials {

    /**
     * Implementors should override this method to perform validation.
     * An argument is allowed to allow custom validation.
     *
     * @param arg optional argument or null.
     * @param <V> the generic type of the argument
     * @throws CredentialValidationException when the validation fails
     */
    default <V> void validate(V arg) throws CredentialValidationException {
    }
}
