package ch.supsi.sporthub.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Base class for all response DTOs in the application.
 * <p>
 * Contains a message field that can be used to convey information about the result
 * of an operation (e.g., success, error details).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Response {
    private String message;
}