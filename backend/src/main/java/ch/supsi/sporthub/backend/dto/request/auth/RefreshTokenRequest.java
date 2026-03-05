package ch.supsi.sporthub.backend.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * RefreshTokenRequest represents the request body used to refresh a user's JWT token.
 * It extends the {@link TokenRequest} class and includes the refresh token and the username of the user requesting the refresh.
 * This request is used when the user needs to get a new access token using their refresh token.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class RefreshTokenRequest extends TokenRequest {
    private final String username;

    /**
     * Constructs an instance of RefreshTokenRequest with the provided refresh token and username.
     *
     * @param token    The refresh token used to request a new access token.
     * @param username The username of the user requesting the token refresh.
     */
    @JsonCreator
    public RefreshTokenRequest(@JsonProperty("token") String token, @JsonProperty("username") String username) {
        super(token);
        this.username = username;
    }
}