package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when a requested resource cannot be found.
 *
 * This is a generic exception used throughout the application to indicate
 * that a specific entity (e.g., user, goal, competition) does not exist or is not accessible.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     *
     * @param message the detail message describing the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}