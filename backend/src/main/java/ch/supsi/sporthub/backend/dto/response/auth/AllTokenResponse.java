package ch.supsi.sporthub.backend.dto.response.auth;

import ch.supsi.sporthub.backend.dto.response.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a response containing the validity status of a token,
 * typically used in authentication-related API endpoints.
 * <p>
 * Extends {@link Response} to include a message and a boolean flag indicating token validity.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AllTokenResponse extends Response {
    private boolean valid;

    /**
     * Constructs a new AllTokenResponse with the given message and validity status.
     *
     * @param message the response message, often describing the validation result
     * @param valid true if the token is valid; false otherwise
     */
    public AllTokenResponse(String message, boolean valid) {
        super(message);
        this.valid = valid;
    }
}