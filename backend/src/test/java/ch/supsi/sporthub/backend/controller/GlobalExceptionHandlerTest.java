package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.dto.response.error.ErrorResponse;
import ch.supsi.sporthub.backend.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleTokenInvalidException() {
        TokenInvalidException ex = new TokenInvalidException("Invalid token");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidToken(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid token", response.getBody().getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleAccountAlreadyActivatedException() {
        AccountAlreadyActivatedException ex = new AccountAlreadyActivatedException("Account already activated");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAlreadyActivated(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Account already activated", response.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleTooManyRequestsException() {
        TooManyRequestsException ex = new TooManyRequestsException("Too many requests");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleTooManyRequests(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Too many requests", response.getBody().getMessage());
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleBadCredentialsException() {
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Illegal argument", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("Runtime error");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleRuntimeException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Runtime error", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Generic error");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleUsernameAlreadyTakenException() {
        UsernameAlreadyTakenException ex = new UsernameAlreadyTakenException("Username taken");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUsernameTaken(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username taken", response.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleAccountNotActiveException() {
        AccountNotActiveException ex = new AccountNotActiveException("Account not active");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAccountNotActive(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Account not active", response.getBody().getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleStravaTokenUnavailableException() {
        StravaTokenUnavailableException ex = new StravaTokenUnavailableException("Strava token unavailable");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleStravaTokenUnavailable(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Strava token unavailable", response.getBody().getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Resource not found");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Resource not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleUnsupportedEventTypeException() {
        UnsupportedEventTypeException ex = new UnsupportedEventTypeException("Unsupported event type");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnsupportedEventType(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Unsupported event type", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleInvalidVerifyTokenException() {
        InvalidVerifyTokenException ex = new InvalidVerifyTokenException("Invalid verify token");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidVerifyToken(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Invalid verify token", response.getBody().getMessage());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleGoalNotFoundException() {
        GoalNotFoundException ex = new GoalNotFoundException("Goal not found");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGoalNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Goal not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleMissingAuthorizationCodeException() {
        MissingAuthorizationCodeException ex = new MissingAuthorizationCodeException("Missing auth code");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMissingAuthorizationCode(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing auth code", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleInvalidProfileImageException() {
        InvalidProfileImageException ex = new InvalidProfileImageException("Invalid image");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidProfileImage(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid image", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleUserImageNotFoundException() {
        UserImageNotFoundException ex = new UserImageNotFoundException("Image not found");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUserImageNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Image not found", response.getBody().getMessage());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleInvalidAuthorizationHeaderException() {
        InvalidAuthorizationHeaderException ex = new InvalidAuthorizationHeaderException("Invalid header");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidAuthHeader(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid header", response.getBody().getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleHttpRequestMethodNotSupportedException() {
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("GET");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodNotSupported(ex);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("Method Not Allowed", response.getBody().getMessage());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleHttpMediaTypeNotSupportedException() {
        HttpMediaTypeNotSupportedException ex = new HttpMediaTypeNotSupportedException("Unsupported media type");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMediaTypeNotSupported(ex);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());
        assertEquals("Unsupported Media Type", response.getBody().getMessage());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleEmailAlreadyTakenException() {
        EmailAlreadyTakenException ex = new EmailAlreadyTakenException("Email taken");
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleEmailTaken(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email taken", response.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleResponseStatusException() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request");
        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleResponseStatusException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request", response.getBody().get("message"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().get("statusCode"));
    }
}