package ch.supsi.sporthub.backend.exception;


/**
 * Exception thrown when an event type received by the system is not supported.
 * This is typically used in webhook handlers or event-driven components
 * to handle unknown or unrecognized event types gracefully.
 */
public class UnsupportedEventTypeException extends RuntimeException {

    /**
     * Constructs a new UnsupportedEventTypeException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public UnsupportedEventTypeException(String message) {
        super(message);
    }
}