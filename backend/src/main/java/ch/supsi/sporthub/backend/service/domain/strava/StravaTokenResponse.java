package ch.supsi.sporthub.backend.service.domain.strava;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the response from Strava's token endpoint after a successful
 * token request (either authorization code exchange or refresh token request).
 * Contains the access token, refresh token, and additional information about the athlete.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StravaTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("expires_at")
    private long expiresAt;

    @JsonProperty("athlete")
    private Athlete athlete;

}