package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.security.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service class for managing JWT tokens related to user authentication and authorization.
 * This service provides methods for generating and validating various types of JWT tokens, such as access tokens,
 * refresh tokens, password reset tokens, and activation tokens. It utilizes the JwtUtil utility class to interact
 * with the JWT tokens and perform necessary actions.
 */
@Service
public class JwtTokenService {
    private final JwtUtil jwtUtil;

    /**
     * Constructor for the JwtTokenService class.
     * Initializes the service with the provided JwtUtil instance for token generation and validation.
     *
     * @param jwtUtil The utility class for generating and validating JWT tokens.
     */
    public JwtTokenService(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Generates an access token for the provided user details.
     * The token is created using the JwtUtil utility class.
     *
     * @param userDetails The details of the user for whom the access token is generated.
     * @return The generated access token as a string.
     */
    public String generateAccessToken(UserDetails userDetails) {
        return jwtUtil.generateToken(userDetails);
    }

    /**
     * Generates a refresh token for the provided user details.
     * The token is created using the JwtUtil utility class.
     *
     * @param userDetails The details of the user for whom the refresh token is generated.
     * @return The generated refresh token as a string.
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return jwtUtil.generateRefreshToken(userDetails);
    }

    /**
     * Generates a password reset token for the provided user details.
     * The token is created using the JwtUtil utility class.
     *
     * @param userDetails The details of the user for whom the password reset token is generated.
     * @return The generated password reset token as a string.
     */
    public String generatePasswordResetToken(UserDetails userDetails) {
        return jwtUtil.generatePasswordResetToken(userDetails);
    }

    /**
     * Generates an account activation token for the provided user details.
     * The token is created using the JwtUtil utility class.
     *
     * @param userDetails The details of the user for whom the account activation token is generated.
     * @return The generated activation token as a string.
     */
    public String generateActivationToken(UserDetails userDetails) {
        return jwtUtil.generateActivationToken(userDetails);
    }

    /**
     * Extracts the username from the provided JWT token.
     * The username is extracted from the token using the JwtUtil utility class.
     *
     * @param token The JWT token from which the username is extracted.
     * @return The username extracted from the token.
     */
    public String extractUsername(String token) {
        return jwtUtil.extractUsername(token);
    }

    /**
     * Validates a password reset token by checking its validity using the JwtUtil utility class.
     *
     * @param token The password reset token to be validated.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validatePasswordResetToken(String token) {
        return jwtUtil.validatePasswordResetToken(token);
    }

    /**
     * Validates an activation token by checking its validity using the JwtUtil utility class.
     *
     * @param token The activation token to be validated.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateActivationToken(String token) {
        return jwtUtil.validateActivationToken(token);
    }

    /**
     * Validates the provided token based on its purpose (access, refresh, password reset, or activation).
     * The method uses the JwtUtil utility class to validate the token accordingly.
     *
     * @param token        The JWT token to be validated.
     * @param userDetails  The user details to validate the token against (used for access and refresh tokens).
     * @return true if the token is valid, false otherwise.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String purpose = jwtUtil.getTokenPurpose(token);
        boolean isValid;

        switch (purpose) {
            case "ACCESS":
            case "REFRESH":
                isValid = jwtUtil.validateToken(token, userDetails);
                break;
            case "PASSWORD_RESET":
                isValid = jwtUtil.validatePasswordResetToken(token);
                break;
            case "ACTIVATION":
                isValid = jwtUtil.validateActivationToken(token);
                break;
            default:
                isValid = false;
        }
        return isValid;
    }

    /**
     * Retrieves the purpose of the provided JWT token (access, refresh, password reset, or activation).
     *
     * @param token The JWT token whose purpose is to be retrieved.
     * @return The purpose of the token (e.g., "ACCESS", "REFRESH", "PASSWORD_RESET", or "ACTIVATION").
     */
    public String getTokenPurpose(String token) {
        return jwtUtil.getTokenPurpose(token);
    }

    /**
     * Validates a refresh token by checking its validity using the JwtUtil utility class and the provided user details.
     *
     * @param refreshToken The refresh token to be validated.
     * @param userDetails  The user details to validate the token against.
     * @return true if the refresh token is valid, false otherwise.
     */
    public boolean isRefreshTokenValid(String refreshToken, UserDetails userDetails) {
        return jwtUtil.validateToken(refreshToken, userDetails);
    }
}