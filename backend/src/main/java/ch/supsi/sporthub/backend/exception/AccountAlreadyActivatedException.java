package ch.supsi.sporthub.backend.exception;


/**
 * Exception thrown when an attempt is made to activate an account
 * that has already been activated.
 * This is a runtime exception typically used to prevent redundant or invalid
 * activation logic in the authentication or user management flow.
 */
public class AccountAlreadyActivatedException extends RuntimeException {

    /**
     * Constructs a new AccountAlreadyActivatedException with the specified detail message.
     * @param message the detail message explaining the reason for the exception
     */
    public AccountAlreadyActivatedException(String message) {
        super(message);
    }
}