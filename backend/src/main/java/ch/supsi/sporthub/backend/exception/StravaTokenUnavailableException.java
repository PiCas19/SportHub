package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when a valid Strava token is not available for a user.
 *
 * This typically occurs when the token has expired, is missing, or cannot be retrieved
 * due to authorization issues with the Strava API.
 */
public class StravaTokenUnavailableException extends RuntimeException {

    /**
     * Constructs a new StravaTokenUnavailableException with the specified detail message.
     *
     * @param message the detail message describing the issue with the Strava token
     */
    public StravaTokenUnavailableException(String message) {
        super(message);
    }
}