package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.exception.*;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;

import java.util.Base64;

/**
 * BaseController serves as a common base for controllers that require JWT authentication.
 * It provides utility methods for extracting and validating JWT tokens and getting the associated user from the token.
 */
public abstract class BaseController {

    protected final JwtTokenService jwtTokenService;
    protected final UserService userService;

    /**
     * Constructs a new BaseController instance with the specified JwtTokenService and UserService.
     *
     * @param jwtTokenService The JwtTokenService for handling JWT-related operations.
     * @param userService     The UserService for interacting with user data.
     */
    protected BaseController(JwtTokenService jwtTokenService, UserService userService) {
        this.jwtTokenService = jwtTokenService;
        this.userService = userService;
    }

    /**
     * Extracts the user associated with the given authorization header.
     * It extracts the JWT token from the header, validates it, and retrieves the user from the username in the token.
     *
     * @param authHeader The authorization header containing the JWT token.
     * @return The User associated with the JWT token.
     * @throws TokenInvalidException If the token format is invalid or an error occurs while processing it.
     * @throws UserNotFoundException If no user is found for the extracted username.
     */
    protected User getUserFromHeader(String authHeader) {
        String token = extractToken(authHeader);

        if (!isValidJwtFormat(token)) {
            throw new TokenInvalidException("Invalid JWT format");
        }

        try {
            String username = jwtTokenService.extractUsername(token);
            User user = userService.findByUsername(username);

            if (user == null) {
                throw new UserNotFoundException("User not found");
            }

            return user;
        } catch (Exception e) {
            throw new TokenInvalidException("Unexpected error processing JWT: " + e.getMessage());
        }
    }

    /**
     * Extracts the JWT token from the authorization header.
     * The header must start with "Bearer " followed by the token.
     *
     * @param authHeader The authorization header.
     * @return The extracted JWT token.
     * @throws InvalidAuthorizationHeaderException If the authorization header is missing or malformed.
     */
    protected String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new InvalidAuthorizationHeaderException("Missing or invalid authorization header");
        }
        return authHeader.substring(7);
    }

    /**
     * Validates if the given token has the correct JWT format (three parts separated by periods).
     * It checks if the token can be decoded into valid Base64 segments.
     *
     * @param token The JWT token to validate.
     * @return true if the token has a valid JWT format, false otherwise.
     */
    private boolean isValidJwtFormat(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }

        try {
            Base64.getUrlDecoder().decode(parts[0]);
            Base64.getUrlDecoder().decode(parts[1]);
        } catch (IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}