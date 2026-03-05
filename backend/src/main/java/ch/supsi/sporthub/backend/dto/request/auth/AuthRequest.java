package ch.supsi.sporthub.backend.dto.request.auth;

import ch.supsi.sporthub.backend.dto.request.Request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AuthRequest represents the request body used for user authentication and registration.
 * It extends the {@link Request} class and includes the necessary fields for creating a user account
 * or authenticating an existing user.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuthRequest extends Request {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String username;
}