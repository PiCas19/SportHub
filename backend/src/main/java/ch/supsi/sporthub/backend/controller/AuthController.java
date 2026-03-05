package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.request.auth.*;
import ch.supsi.sporthub.backend.dto.response.auth.AllTokenResponse;
import ch.supsi.sporthub.backend.dto.response.Response;
import ch.supsi.sporthub.backend.dto.response.auth.AuthResponse;
import ch.supsi.sporthub.backend.exception.*;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.EmailService;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * The AuthController class handles authentication-related requests such as login, registration,
 * token validation, password reset, and account activation.
 */
@RestController
@RequestMapping("/auth")
public class AuthController extends BaseController {

    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final UserDetailsService userDetailsService;

    /**
     * Constructs an instance of AuthController with the required services.
     *
     * @param authenticationManager The AuthenticationManager to authenticate users.
     * @param jwtTokenService       The JwtTokenService for generating and validating tokens.
     * @param userService           The UserService to interact with user data.
     * @param userDetailsService    The UserDetailsService to load user details.
     * @param emailService          The EmailService for sending activation and password reset emails.
     */
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenService jwtTokenService,
                          UserService userService,
                          UserDetailsService userDetailsService,
                          EmailService emailService) {
        super(jwtTokenService, userService);
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.emailService = emailService;
    }

    /**
     * Authenticates the user and returns an access token and refresh token.
     *
     * @param request The authentication request containing the username and password.
     * @return A response entity containing the authentication status and tokens.
     * @throws UserNotFoundException If the user is not found.
     * @throws AccountNotActiveException If the account is not activated.
     */
    @PostMapping("/login")
    public ResponseEntity<Response> login(@RequestBody AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userService.findByUsername(request.getUsername());
        if (user == null) throw new UserNotFoundException("User not found");

        if (!user.getAccountStatus())
            throw new AccountNotActiveException("Account not active. Please activate it via email.");

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

        return ResponseEntity.ok(new AuthResponse("Login successful", accessToken, refreshToken));
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param request The request containing the refresh token.
     * @return A response entity containing the new access token.
     * @throws TokenInvalidException If the refresh token is invalid or expired.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Response> refresh(@RequestBody RefreshTokenRequest request) {
        String token = request.getToken();
        String username = jwtTokenService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtTokenService.isRefreshTokenValid(token, userDetails)) {
            throw new TokenInvalidException("Invalid or expired refresh token");
        }

        String newAccessToken = jwtTokenService.generateAccessToken(userDetails);
        return ResponseEntity.ok(new AuthResponse("Token refreshed", newAccessToken, token));
    }

    /**
     * Registers a new user and sends an email with an activation link.
     *
     * @param request The registration request containing user details.
     * @param role    The role assigned to the user (default is "USER").
     * @return A response entity containing the registration status and tokens.
     * @throws UsernameAlreadyTakenException If the username is already taken.
     */
    @PostMapping("/register")
    public ResponseEntity<Response> register(@RequestBody AuthRequest request,
                                             @RequestParam(defaultValue = "USER") String role) {
        if (userService.findByUsername(request.getUsername()) != null) {
            throw new UsernameAlreadyTakenException("Username already taken");
        }

        User user = userService.registerUser(request.getUsername(), request.getPassword(), role,
                request.getEmail(), request.getFirstName(), request.getLastName());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String activationToken = jwtTokenService.generateActivationToken(userDetails);
        String activationLink = "http://localhost:5173/activate?token=" + activationToken;
        emailService.sendActivationCode(user.getEmail(), activationLink);

        String accessToken = jwtTokenService.generateAccessToken(userDetails);
        String refreshToken = jwtTokenService.generateRefreshToken(userDetails);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse("User registered. Activation email sent.", accessToken, refreshToken));
    }

    /**
     * Activates the user account using the provided activation token.
     *
     * @param request The request containing the activation token.
     * @return A response entity indicating the activation status.
     * @throws TokenInvalidException If the activation token is invalid or expired.
     * @throws UserNotFoundException If the user is not found.
     * @throws AccountAlreadyActivatedException If the account is already activated.
     */
    @PostMapping("/activate")
    public ResponseEntity<Response> activateAccount(@RequestBody ActivateTokenRequest request) {
        if (!jwtTokenService.validateActivationToken(request.getToken())) {
            throw new TokenInvalidException("Invalid or expired activation token");
        }

        String username = jwtTokenService.extractUsername(request.getToken());
        User user = userService.findByUsername(username);
        if (user == null) throw new UserNotFoundException("User not found");

        if (user.getAccountStatus()) {
            throw new AccountAlreadyActivatedException("Account already activated");
        }

        userService.activateUser(user);
        return ResponseEntity.noContent().build();
    }

    /**
     * Validates the provided token and checks its validity.
     *
     * @param token The token to validate.
     * @return A response entity indicating whether the token is valid.
     * @throws TokenInvalidException If the token is invalid or expired.
     */
    @GetMapping("/validate-token")
    public ResponseEntity<Response> validateToken(@RequestParam("token") String token) {
        String purpose = jwtTokenService.getTokenPurpose(token);
        String username = jwtTokenService.extractUsername(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!jwtTokenService.isTokenValid(token, userDetails)) {
            throw new TokenInvalidException(purpose + " token is invalid or expired for user: " + username);
        }

        return ResponseEntity.ok(new AllTokenResponse(purpose + " token valid for " + username, true));
    }

    /**
     * Logs out the user by invalidating their session or token.
     *
     * @return A response entity indicating the logout status.
     */
    @PostMapping("/logout")
    public ResponseEntity<Response> logout() {
        return ResponseEntity.ok(new Response("Logout successful"));
    }

    /**
     * Sends a password reset email to the user with the given email address.
     *
     * @param request The request containing the email address.
     * @return A response entity indicating the status of the password reset request.
     * @throws UserNotFoundException If the user is not found with the provided email.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Response> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        User user = userService.findByEmail(request.getEmail());
        if (user == null) throw new UserNotFoundException("User not found");

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtTokenService.generatePasswordResetToken(userDetails);
        String resetLink = "http://localhost:5173/resetPassword?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(new Response("Reset password email sent"));
    }

    /**
     * Resets the user's password using the provided reset token and new password.
     *
     * @param request The request containing the reset token and new password.
     * @return A response entity indicating the status of the password reset.
     * @throws TokenInvalidException If the reset token is invalid or expired.
     * @throws UserNotFoundException If the user is not found.
     */
    @PutMapping("/reset-password")
    public ResponseEntity<Response> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (!jwtTokenService.validatePasswordResetToken(request.getToken())) {
            throw new TokenInvalidException("Invalid or expired token");
        }

        String username = jwtTokenService.extractUsername(request.getToken());
        User user = userService.findByUsername(username);
        if (user == null) throw new UserNotFoundException("User not found");

        userService.updateUserPassword(user, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Changes the user's password using the provided current and new passwords.
     *
     * @param request    The request containing the current and new passwords.
     * @param authHeader The authorization header containing the user's token.
     * @return A response entity indicating the status of the password change.
     */
    @PutMapping("/change-password")
    public ResponseEntity<Response> changePassword(@RequestBody ChangePasswordRequest request,
                                                   @RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getCurrentPassword()));

        userService.updateUserPassword(user, request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the user's account.
     *
     * @param authHeader The authorization header containing the user's token.
     * @return A response entity indicating the status of the account deletion.
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Response> deleteAccount(@RequestHeader("Authorization") String authHeader) {
        User user = getUserFromHeader(authHeader);
        userService.deleteUser(user);
        return ResponseEntity.noContent().build();
    }
}