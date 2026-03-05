package ch.supsi.sporthub.backend.service.domain.strava;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * Represents a base class for making requests to Strava's OAuth token endpoint.
 * This class provides common functionality for generating token requests
 * by including client credentials and grant type.
 */
public abstract class StravaTokenRequest {
    private final String clientId;
    private final String clientSecret;

    /**
     * Constructs a new StravaTokenRequest with the given client ID and client secret.
     *
     * @param clientId The client ID issued by Strava.
     * @param clientSecret The client secret issued by Strava.
     */
    public StravaTokenRequest(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    /**
     * Builds the request body for the token request, including client credentials,
     * grant type, and additional token parameters specific to the grant type.
     *
     * @return A MultiValueMap representing the request body for the token request.
     */
    public MultiValueMap<String, String> buildRequest() {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("grant_type", getGrantType());
        addTokenParameter(requestBody);
        return requestBody;
    }

    /**
     * Returns the specific grant type for the token request.
     * This should be overridden by subclasses to provide the correct grant type
     * (e.g., "authorization_code", "refresh_token").
     *
     * @return The grant type for the token request.
     */
    public abstract String getGrantType();

    /**
     * Adds additional parameters to the token request specific to the grant type.
     * This method should be overridden by subclasses to add the necessary parameters
     * (e.g., authorization code or refresh token).
     *
     * @param requestBody The request body to which the parameters should be added.
     */
    public abstract void addTokenParameter(MultiValueMap<String, String> requestBody);

}