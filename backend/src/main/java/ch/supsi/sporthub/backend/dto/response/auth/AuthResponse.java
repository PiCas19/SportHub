package ch.supsi.sporthub.backend.dto.response.auth;

import ch.supsi.sporthub.backend.dto.response.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * Represents an authentication response containing access and refresh tokens.
 * <p>
 * This response is typically returned after a successful login or token refresh operation.
 * It extends {@link Response} to include a message and two JWT tokens used for
 * authenticated access and token renewal.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AuthResponse extends Response {
    private String accessToken;
    private String refreshToken;

    /**
     * Constructs a new AuthResponse with the given message, access token, and refresh token.
     *
     * @param message the response message, typically confirming successful authentication
     * @param accessToken the JWT access token for short-lived access
     * @param refreshToken the JWT refresh token for renewing access
     */
    public AuthResponse(String message, String accessToken, String refreshToken) {
        super(message);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

}