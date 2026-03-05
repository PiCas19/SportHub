package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when a client sends too many requests in a given amount of time.
 * This exception is typically used to enforce rate limiting or prevent abuse
 * of an API or service by limiting the number of allowed requests.
 */
public class TooManyRequestsException extends RuntimeException {

    /**
     * Constructs a new TooManyRequestsException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public TooManyRequestsException(String message) {
        super(message);
    }
}