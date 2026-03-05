package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when an attempt is made to register or update an account
 * using an email address that is already associated with another account.
 *
 * This exception is typically used during user registration or email update processes
 * to enforce email uniqueness.
 */
public class EmailAlreadyTakenException extends RuntimeException {

    /**
     * Constructs a new EmailAlreadyTakenException with the specified detail message.
     * @param message the detail message explaining why the email is considered taken
     */
    public EmailAlreadyTakenException(String message) {
        super(message);
    }
}