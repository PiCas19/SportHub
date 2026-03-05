package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when a provided token is found to be invalid.
 * This exception is typically used in authentication or authorization flows
 * where a token fails validation due to reasons such as expiration, corruption,
 * or incorrect format.
 */
public class TokenInvalidException extends RuntimeException{

    /**
     * Constructs a new TokenInvalidException with the specified detail message.
     *
     * @param message the detail message explaining why the token is considered invalid
     */
    public TokenInvalidException(String message) {
        super(message);
    }
}