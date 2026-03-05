package ch.supsi.sporthub.backend.dto.response.strava;

import ch.supsi.sporthub.backend.dto.response.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the response containing the Strava authorization URL.
 * <p>
 * This response is typically used when initiating the OAuth flow with Strava.
 * It extends {@link Response} and includes the URL that the client should use
 * to redirect the user for authentication with Strava.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StravaUrlResponse extends Response {
    private String authorizationUrl;

    /**
     * Constructs a response object with a message and a Strava authorization URL.
     *
     * @param message           a brief message describing the result
     * @param authorizationUrl the URL to be used for initiating Strava OAuth
     */
    public StravaUrlResponse(String message, String authorizationUrl) {
        super(message);
        this.authorizationUrl = authorizationUrl;
    }
}