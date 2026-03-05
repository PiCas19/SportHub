package ch.supsi.sporthub.backend.dto.request.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ResetPasswordRequest represents the request body used for resetting a user's password.
 * It extends the {@link TokenRequest} class and includes the new password that the user wants to set.
 * This request is used when a user submits their new password after verifying their identity with a reset token.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ResetPasswordRequest extends TokenRequest {
    private String newPassword;
}