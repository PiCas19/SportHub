package ch.supsi.sporthub.backend.service.domain.strava;

/**
 * Factory class for creating instances of different types of Strava token request.
 * This class provides methods to create requests for obtaining an access token
 * using either an authorization code or a refresh token.
 */
public class TokenRequestFactory {

    /**
     * Creates a new Strava token request using the authorization code flow.
     * This request is used to exchange an authorization code for an access token.
     *
     * @param clientId The client ID issued by Strava.
     * @param clientSecret The client secret issued by Strava.
     * @param code The authorization code obtained from Strava after user authentication.
     * @return A StravaAuthorizationCodeTokenRequest object.
     */
    public static StravaTokenRequest createAuthorizationCodeRequest(String clientId, String clientSecret, String code) {
        return new StravaAuthorizationCodeTokenRequest(clientId, clientSecret, code);
    }

    /**
     * Creates a new Strava token request using the refresh token flow.
     * This request is used to obtain a new access token using an existing refresh token.
     *
     * @param clientId The client ID issued by Strava.
     * @param clientSecret The client secret issued by Strava.
     * @param refreshToken The refresh token used to request a new access token.
     * @return A StravaRefreshTokenRequest object.
     */
    public static StravaTokenRequest createRefreshTokenRequest(String clientId, String clientSecret, String refreshToken) {
        return new StravaRefreshTokenRequest(clientId, clientSecret, refreshToken);
    }
}