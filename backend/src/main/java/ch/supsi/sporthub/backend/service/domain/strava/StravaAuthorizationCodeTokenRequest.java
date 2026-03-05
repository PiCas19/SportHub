package ch.supsi.sporthub.backend.service.domain.strava;

import org.springframework.util.MultiValueMap;

/**
 * Represents a request for an access token using the authorization code flow.
 * This request is made to exchange the authorization code for an access token from Strava.
 */
public class StravaAuthorizationCodeTokenRequest extends StravaTokenRequest {

    private final String authorizationCode;

    /**
     * Constructs a new StravaAuthorizationCodeTokenRequest with the given client ID,
     * client secret, and authorization code.
     *
     * @param clientId The client ID issued by Strava.
     * @param clientSecret The client secret issued by Strava.
     * @param authorizationCode The authorization code received from Strava after user authentication.
     */
    public StravaAuthorizationCodeTokenRequest(String clientId, String clientSecret, String authorizationCode) {
        super(clientId, clientSecret);
        this.authorizationCode = authorizationCode;
    }

    /**
     * Returns the grant type for this request, which is "authorization_code".
     *
     * @return The grant type for authorization code flow.
     */
    @Override
    public String getGrantType() {
        return "authorization_code";
    }

    /**
     * Adds the authorization code as a parameter to the request body.
     * This is required for exchanging the authorization code for an access token.
     *
     * @param requestBody The request body to which the parameters are added.
     */
    @Override
    public void addTokenParameter(MultiValueMap<String, String> requestBody) {
        requestBody.add("code", authorizationCode);
    }
}