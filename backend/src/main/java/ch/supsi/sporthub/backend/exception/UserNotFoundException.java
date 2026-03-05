package ch.supsi.sporthub.backend.exception;


/**
 * Exception thrown when a user is requested but cannot be found
 * in the system (e.g., during login, token validation, or data retrieval).
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}