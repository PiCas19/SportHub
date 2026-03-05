package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.response.error.ErrorResponse;
import ch.supsi.sporthub.backend.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * GlobalExceptionHandler handles exceptions globally for all controllers.
 * It provides custom responses for various exception types, returning appropriate HTTP status codes and messages.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles UserNotFoundException and returns a 404 (Not Found) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 404 status code.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles TokenInvalidException and returns a 401 (Unauthorized) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 401 status code.
     */
    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(TokenInvalidException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles AccountAlreadyActivatedException and returns a 409 (Conflict) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 409 status code.
     */
    @ExceptionHandler(AccountAlreadyActivatedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyActivated(AccountAlreadyActivatedException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Handles TooManyRequestsException and returns a 429 (Too Many Requests) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 429 status code.
     */
    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestsException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Handles BadCredentialsException and returns a 401 (Unauthorized) status code with the message "Invalid credentials".
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 401 status code.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles IllegalArgumentException and returns a 400 (Bad Request) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 400 status code.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles RuntimeException and returns a 500 (Internal Server Error) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 500 status code.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles any generic Exception and returns a 500 (Internal Server Error) status code with a default error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the default error message and 500 status code.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles UsernameAlreadyTakenException and returns a 409 (Conflict) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 409 status code.
     */
    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> handleUsernameTaken(UsernameAlreadyTakenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    /**
     * Handles AccountNotActiveException and returns a 403 (Forbidden) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 403 status code.
     */
    @ExceptionHandler(AccountNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotActive(AccountNotActiveException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    /**
     * Handles StravaTokenUnavailableException and returns a 401 (Unauthorized) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 401 status code.
     */
    @ExceptionHandler(StravaTokenUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleStravaTokenUnavailable(StravaTokenUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    /**
     * Handles ResourceNotFoundException and returns a 404 (Not Found) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 404 status code.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Handles UnsupportedEventTypeException and returns a 400 (Bad Request) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 400 status code.
     */
    @ExceptionHandler(UnsupportedEventTypeException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedEventType(UnsupportedEventTypeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value()));
    }

    /**
     * Handles InvalidVerifyTokenException and returns a 403 (Forbidden) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 403 status code.
     */
    @ExceptionHandler(InvalidVerifyTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerifyToken(InvalidVerifyTokenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN.value()));
    }

    /**
     * Handles GoalNotFoundException and returns a 404 (Not Found) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 404 status code.
     */
    @ExceptionHandler(GoalNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleGoalNotFound(GoalNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }

    /**
     * Handles MissingAuthorizationCodeException and returns a 400 (Bad Request) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 400 status code.
     */
    @ExceptionHandler(MissingAuthorizationCodeException.class)
    public ResponseEntity<ErrorResponse> handleMissingAuthorizationCode(MissingAuthorizationCodeException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles InvalidProfileImageException and returns a 400 (Bad Request) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 400 status code.
     */
    @ExceptionHandler(InvalidProfileImageException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProfileImage(InvalidProfileImageException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles UserImageNotFoundException and returns a 404 (Not Found) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 404 status code.
     */
    @ExceptionHandler(UserImageNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserImageNotFound(UserImageNotFoundException ex) {
        return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles InvalidAuthorizationHeaderException and returns a 401 (Unauthorized) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 401 status code.
     */
    @ExceptionHandler(InvalidAuthorizationHeaderException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAuthHeader(InvalidAuthorizationHeaderException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    /**
     * Handles HttpRequestMethodNotSupportedException and returns a 405 (Method Not Allowed) status code with a default error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with a default error message and 405 status code.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse("Method Not Allowed", HttpStatus.METHOD_NOT_ALLOWED.value());
        return new ResponseEntity<>(error, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * Handles HttpMediaTypeNotSupportedException and returns a 415 (Unsupported Media Type) status code with a default error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with a default error message and 415 status code.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        ErrorResponse error = new ErrorResponse("Unsupported Media Type", HttpStatus.UNSUPPORTED_MEDIA_TYPE.value());
        return new ResponseEntity<>(error, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handles EmailAlreadyTakenException and returns a 409 (Conflict) status code with the error message.
     *
     * @param ex The exception to handle.
     * @return A response entity with the error message and 409 status code.
     */
    @ExceptionHandler(EmailAlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> handleEmailTaken(EmailAlreadyTakenException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    /**
     * Handles ResponseStatusException and returns a response entity with a map containing the error message and status code.
     *
     * @param ex The exception to handle.
     * @return A response entity with a map containing the error message and status code.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getReason());
        body.put("statusCode", ex.getStatusCode().value());

        return new ResponseEntity<>(body, ex.getStatusCode());
    }

    /**
     * Helper method to build a response entity with an error message and HTTP status code.
     *
     * @param message The error message.
     * @param status  The HTTP status code.
     * @return A response entity with the error message and status code.
     */
    private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResponse(message, status.value()));
    }
}