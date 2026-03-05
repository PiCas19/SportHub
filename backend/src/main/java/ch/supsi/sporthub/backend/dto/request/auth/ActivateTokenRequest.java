package ch.supsi.sporthub.backend.dto.request.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * ActivateTokenRequest represents the request body used for activating a user's account.
 * It extends the {@link TokenRequest} class and includes a token field, which is required for account activation.
 * The token is typically received via email as part of an account activation process.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
public class ActivateTokenRequest extends TokenRequest {

    /**
     * Constructs an instance of ActivateTokenRequest with the provided activation token.
     *
     * @param token The token used for activating the user's account.
     */
    @JsonCreator
    public ActivateTokenRequest(@JsonProperty("token") String token) {
        super(token);
    }
}