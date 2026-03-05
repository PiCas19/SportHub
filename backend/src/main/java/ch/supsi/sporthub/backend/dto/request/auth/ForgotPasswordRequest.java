package ch.supsi.sporthub.backend.dto.request.auth;
import ch.supsi.sporthub.backend.dto.request.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ForgotPasswordRequest represents the request body used when a user forgets their password and requests a password reset.
 * It extends the {@link Request} class and includes the user's email address to send a password reset link.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ForgotPasswordRequest extends Request {
    private String email;
}