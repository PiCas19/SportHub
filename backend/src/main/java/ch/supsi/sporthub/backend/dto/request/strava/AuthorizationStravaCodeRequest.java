package ch.supsi.sporthub.backend.dto.request.strava;

import ch.supsi.sporthub.backend.dto.request.Request;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AuthorizationStravaCodeRequest is a data transfer object (DTO) used to encapsulate
 * the authorization code received from Strava's OAuth flow.
 * <p>
 * This request object is typically used when exchanging the authorization code
 * for an access token.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthorizationStravaCodeRequest extends Request {
    private String code;

    /**
     * Constructs a new AuthorizationStravaCodeRequest with the provided code.
     *
     * @param code The authorization code returned by Strava after user authentication.
     */
    @JsonCreator
    public AuthorizationStravaCodeRequest(@JsonProperty("code") String code) {
        this.code = code;
    }
}