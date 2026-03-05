package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when an expected authorization code is missing from the request.
 * This exception is typically used in OAuth or external authentication flows
 * where a code (e.g. from Strava) is required to complete the authorization process.
 */
public class MissingAuthorizationCodeException extends RuntimeException {

    /**
     * Constructs a new MissingAuthorizationCodeException with the specified detail message.
     *
     * @param message the detail message explaining why the exception was thrown
     */
    public MissingAuthorizationCodeException(String message) {
        super(message);
    }
}