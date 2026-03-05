package ch.supsi.sporthub.backend.security;

import ch.supsi.sporthub.backend.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility class for generating and validating JWT tokens used for authentication,
 * authorization, account activation, and password reset.
 */
@Component
public class JwtUtil {

    private final JwtProperties jwtProperties;

    /**
     * Constructor for dependency injection of JWT configuration properties.
     *
     * @param jwtProperties the properties used to configure token behavior and secrets
     */
    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    /**
     * Generates a signing key from the configured secret key.
     *
     * @return a {@link SecretKey} used to sign the JWT
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates an access token for the specified user.
     *
     * @param userDetails the authenticated user details
     * @return a JWT access token
     */
    public String generateToken(UserDetails userDetails) {
        return createToken(userDetails.getUsername(), jwtProperties.getExpirationTime(), "ACCESS");
    }

    /**
     * Generates a refresh token for the specified user.
     *
     * @param userDetails the authenticated user details
     * @return a JWT refresh token
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return createToken(userDetails.getUsername(), jwtProperties.getRefreshExpirationTime(), "REFRESH");
    }

    /**
     * Generates a password reset token for the specified user.
     *
     * @param userDetails the authenticated user details
     * @return a JWT token for password reset
     */
    public String generatePasswordResetToken(UserDetails userDetails) {
        return createToken(userDetails.getUsername(), jwtProperties.getResetExpirationTime(), "PASSWORD_RESET");
    }

    /**
     * Generates an account activation token for the specified user.
     *
     * @param userDetails the authenticated user details
     * @return a JWT token for account activation
     */
    public String generateActivationToken(UserDetails userDetails) {
        return createToken(userDetails.getUsername(), jwtProperties.getActivationExpirationTime(), "ACTIVATION");
    }

    /**
     * Internal method to create a token with specified parameters.
     *
     * @param username       the subject (username)
     * @param expirationTime the duration in milliseconds before the token expires
     * @param purpose        a string describing the token's purpose
     * @return a signed JWT
     */
    private String createToken(String username, long expirationTime, String purpose) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .claim("purpose", purpose)
                .signWith(getSignKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Extracts the purpose of the given JWT token.
     *
     * @param token the JWT token
     * @return the purpose string
     */
    public String getTokenPurpose(String token) {
        return extractClaim(token, claims -> claims.get("purpose", String.class));
    }

    /**
     * Validates a password reset token.
     *
     * @param token the JWT token
     * @return true if valid and of type PASSWORD_RESET, false otherwise
     */
    public boolean validatePasswordResetToken(String token) {
        return extractClaim(token, Claims::getExpiration).after(new Date())
                && "PASSWORD_RESET".equals(extractClaim(token, claims -> claims.get("purpose", String.class)));
    }

    /**
     * Validates an activation token.
     *
     * @param token the JWT token
     * @return true if valid and of type ACTIVATION, false otherwise
     */
    public boolean validateActivationToken(String token) {
        return extractClaim(token, Claims::getExpiration).after(new Date())
                && "ACTIVATION".equals(extractClaim(token, claims -> claims.get("purpose", String.class)));
    }

    /**
     * Validates an access or refresh token based on the user details.
     *
     * @param token        the JWT token
     * @param userDetails  the user details to verify
     * @return true if token is valid and matches user, false otherwise
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT token
     * @return the subject (username)
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the JWT token.
     *
     * @param token the JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if a token is expired.
     *
     * @param token the JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts a specific claim from the JWT token using the provided resolver.
     *
     * @param token          the JWT token
     * @param claimsResolver function to extract a specific claim
     * @param <T>            the type of the claim
     * @return extracted claim
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token);
            return claimsResolver.apply(claimsJws.getPayload());
        } catch (ExpiredJwtException e) {
            throw new ExpiredJwtException(null, null, "JWT token has expired", e);
        } catch (UnsupportedJwtException e) {
            throw new UnsupportedJwtException("Unsupported JWT token", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new MalformedJwtException("Invalid JWT token", e);
        }
    }
}