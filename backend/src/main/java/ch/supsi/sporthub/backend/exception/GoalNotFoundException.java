package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when a goal with the specified identifier is not found in the system.
 * This exception is typically used in goal-related operations such as retrieval,
 * update, or deletion, to indicate that the requested goal does not exist.
 */
public class GoalNotFoundException extends RuntimeException {
    /**
     * Constructs a new GoalNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining the context of the exception
     */
    public GoalNotFoundException(String message) {
        super(message);
    }
}