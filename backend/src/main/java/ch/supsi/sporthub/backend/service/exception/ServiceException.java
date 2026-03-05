package ch.supsi.sporthub.backend.service.exception;

/**
 * Custom exception class for handling service-related errors.
 * This exception is a subclass of {@link RuntimeException}, and is thrown to indicate
 * an error condition within the service layer of the application.
 * The exception includes constructors for providing a message and an optional cause,
 * allowing for flexible error handling and debugging.
 */
public class ServiceException extends RuntimeException {

    /**
     * Constructs a new {@link ServiceException} with the specified detail message.
     *
     * @param message The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@link ServiceException} with the specified detail message and cause.
     *
     * @param message The detail message, which is saved for later retrieval by the {@link #getMessage()} method.
     * @param cause   The cause of the exception, which is saved for later retrieval by the {@link #getCause()} method.
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}