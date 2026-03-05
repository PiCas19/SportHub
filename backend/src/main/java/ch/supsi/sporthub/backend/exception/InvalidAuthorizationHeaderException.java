package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when the authorization header in an HTTP request is invalid or malformed.
 * This exception is typically used in authentication or security filters to signal
 * that the expected format of the Authorization header (e.g., "Bearer <token>") was not respected.
 */
public class InvalidAuthorizationHeaderException extends RuntimeException {

    /**
     * Constructs a new InvalidAuthorizationHeaderException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidAuthorizationHeaderException(String message) {
        super(message);
    }
}