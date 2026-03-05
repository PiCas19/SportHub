package ch.supsi.sporthub.backend.dto.request.auth;

import ch.supsi.sporthub.backend.dto.request.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ChangePasswordRequest represents the request body used for changing a user's password.
 * It extends the {@link Request} class and includes the user's current password and the new password.
 * This request is typically used when a user wants to change their password during authentication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ChangePasswordRequest extends Request {
    private String currentPassword;
    private String newPassword;
}