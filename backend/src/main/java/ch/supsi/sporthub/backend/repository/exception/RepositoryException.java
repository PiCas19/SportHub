package ch.supsi.sporthub.backend.repository.exception;

/**
 * Custom exception class to indicate errors occurring within the repository layer.
 * <p>
 * This exception can be thrown when a data access or loading operation fails,
 * such as reading from a file or loading image resources.
 */
public class RepositoryException extends Exception {

    /**
     * Constructs a new {@code RepositoryException} with the specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code RepositoryException} with the specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause   the underlying cause of the exception
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}