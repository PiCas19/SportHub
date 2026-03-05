package ch.supsi.sporthub.backend.service.domain.strava;

import org.springframework.util.MultiValueMap;

/**
 * Represents a request for refreshing an access token using a refresh token.
 * This request is made to exchange a refresh token for a new access token from Strava.
 */
public class StravaRefreshTokenRequest extends StravaTokenRequest {

    private final String refreshToken;

    /**
     * Constructs a new StravaRefreshTokenRequest with the given client ID,
     * client secret, and refresh token.
     *
     * @param clientId     The client ID issued by Strava.
     * @param clientSecret The client secret issued by Strava.
     * @param refreshToken The refresh token used to obtain a new access token.
     */
    public StravaRefreshTokenRequest(String clientId, String clientSecret, String refreshToken) {
        super(clientId, clientSecret);
        this.refreshToken = refreshToken;
    }

    /**
     * Returns the grant type for this request, which is "refresh_token".
     *
     * @return The grant type for refreshing the token.
     */
    @Override
    public String getGrantType() {
        return "refresh_token";
    }

    /**
     * Adds the refresh token as a parameter to the request body.
     * This is required for refreshing the access token using the provided refresh token.
     *
     * @param requestBody The request body to which the parameters are added.
     */
    @Override
    public void addTokenParameter(MultiValueMap<String, String> requestBody) {
        requestBody.add("refresh_token", refreshToken);
    }
}