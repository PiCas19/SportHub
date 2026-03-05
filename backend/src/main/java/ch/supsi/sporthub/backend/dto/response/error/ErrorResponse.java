package ch.supsi.sporthub.backend.dto.response.error;

import ch.supsi.sporthub.backend.dto.response.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Represents a standardized error response returned by the API when a request fails.
 * <p>
 * Inherits from {@link Response} and adds an HTTP status code to indicate the type of error.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ErrorResponse extends Response {
    private int statusCode;

    /**
     * Constructs an ErrorResponse with a specific message and HTTP status code.
     *
     * @param message a human-readable description of the error
     * @param statusCode the HTTP status code that describes the error type
     */
    public ErrorResponse(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

}