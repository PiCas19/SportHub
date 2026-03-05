package ch.supsi.sporthub.backend.dto.request.auth;

import ch.supsi.sporthub.backend.dto.request.Request;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * TokenRequest is an abstract class that represents a request body containing a token.
 * This class is extended by other request classes that require a token, such as
 * {@link ActivateTokenRequest}, {@link RefreshTokenRequest}, and {@link ResetPasswordRequest}.
 * The token is typically used for authentication, authorization, or verification purposes.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public abstract class TokenRequest extends Request {
    private String token;

    /**
     * Constructs an instance of TokenRequest with the specified token.
     *
     * @param token The token to be used for authentication, authorization, or verification.
     */
    @JsonCreator
    public TokenRequest(@JsonProperty("token") String token) {
        this.token = token;
    }
}