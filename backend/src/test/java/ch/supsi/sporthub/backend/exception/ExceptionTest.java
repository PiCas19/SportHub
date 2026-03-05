package ch.supsi.sporthub.backend.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionTest {

    @Test void testAccountAlreadyActivatedException() {
        AccountAlreadyActivatedException ex = new AccountAlreadyActivatedException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testAccountNotActiveException() {
        AccountNotActiveException ex = new AccountNotActiveException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testEmailAlreadyTakenException() {
        EmailAlreadyTakenException ex = new EmailAlreadyTakenException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testGoalNotFoundException() {
        GoalNotFoundException ex = new GoalNotFoundException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testInvalidAuthorizationHeaderException() {
        InvalidAuthorizationHeaderException ex = new InvalidAuthorizationHeaderException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testInvalidProfileImageException() {
        InvalidProfileImageException ex = new InvalidProfileImageException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testInvalidVerifyTokenException() {
        InvalidVerifyTokenException ex = new InvalidVerifyTokenException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testMissingAuthorizationCodeException() {
        MissingAuthorizationCodeException ex = new MissingAuthorizationCodeException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testStravaTokenUnavailableException() {
        StravaTokenUnavailableException ex = new StravaTokenUnavailableException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testTokenInvalidException() {
        TokenInvalidException ex = new TokenInvalidException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testTooManyRequestsException() {
        TooManyRequestsException ex = new TooManyRequestsException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testUnsupportedEventTypeException() {
        UnsupportedEventTypeException ex = new UnsupportedEventTypeException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testUserImageNotFoundException() {
        UserImageNotFoundException ex = new UserImageNotFoundException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testUsernameAlreadyTakenException() {
        UsernameAlreadyTakenException ex = new UsernameAlreadyTakenException("msg");
        assertEquals("msg", ex.getMessage());
    }

    @Test void testUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("msg");
        assertEquals("msg", ex.getMessage());
    }
}
