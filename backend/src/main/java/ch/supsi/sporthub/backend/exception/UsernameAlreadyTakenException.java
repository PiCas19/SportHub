package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when a user attempts to register or update their profile
 * with a username that is already taken by another user.
 */
public class UsernameAlreadyTakenException extends RuntimeException {

    /**
     * Constructs a new UsernameAlreadyTakenException with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public UsernameAlreadyTakenException(String message) {
        super(message);
    }
}