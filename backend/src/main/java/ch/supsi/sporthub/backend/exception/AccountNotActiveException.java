package ch.supsi.sporthub.backend.exception;


/**
 * Exception thrown when an operation is attempted on an account
 * that has not yet been activated.
 * This exception is typically used in authentication or authorization logic
 * to enforce that only active accounts can perform certain actions.
 */
public class AccountNotActiveException extends RuntimeException {
    /**
     * Constructs a new AccountNotActiveException with the specified detail message.
     * @param message the detail message explaining why the account is considered inactive
     */
    public AccountNotActiveException(String message) {
        super(message);
    }
}