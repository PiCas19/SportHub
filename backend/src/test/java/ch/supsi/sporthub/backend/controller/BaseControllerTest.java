package ch.supsi.sporthub.backend.controller;

import ch.supsi.sporthub.backend.exception.*;
import ch.supsi.sporthub.backend.model.User;
import ch.supsi.sporthub.backend.service.JwtTokenService;
import ch.supsi.sporthub.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BaseControllerTest {

    private static class TestableBaseController extends BaseController {
        public TestableBaseController(JwtTokenService jwtTokenService, UserService userService) {
            super(jwtTokenService, userService);
        }
    }

    private static final String VALID_JWT = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final String VALID_USERNAME = "testuser";

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserService userService;

    private TestableBaseController baseController;

    @BeforeEach
    void setUp() {
        baseController = new TestableBaseController(jwtTokenService, userService);
    }

    @Test
    void constructor_ShouldInitializeServices() {
        assertNotNull(baseController);
        assertSame(jwtTokenService, baseController.jwtTokenService);
        assertSame(userService, baseController.userService);
    }

    @Test
    void extractToken_WithValidHeader_ShouldReturnToken() {
        String token = baseController.extractToken("Bearer " + VALID_JWT);
        assertEquals(VALID_JWT, token);
    }

    @Test
    void extractToken_WithNullHeader_ShouldThrowException() {
        assertThrows(InvalidAuthorizationHeaderException.class,
                () -> baseController.extractToken(null));
    }

    @Test
    void extractToken_WithInvalidPrefix_ShouldThrowException() {
        assertThrows(InvalidAuthorizationHeaderException.class,
                () -> baseController.extractToken("Invalid " + VALID_JWT));
    }


    @Test
    void isValidJwtFormat_WithValidJWT_ShouldReturnTrue() throws Exception {
        assertTrue(invokePrivateIsValidJwtFormat(VALID_JWT));
    }

    @Test
    void isValidJwtFormat_WithInvalidPartCount_ShouldReturnFalse() throws Exception {
        assertFalse(invokePrivateIsValidJwtFormat("invalid.partcount"));
    }

    @Test
    void isValidJwtFormat_WithInvalidBase64_ShouldReturnFalse() throws Exception {
        assertFalse(invokePrivateIsValidJwtFormat("invalid~base64~part"));
    }

    @Test
    void isValidJwtFormat_WithEmptyToken_ShouldReturnFalse() throws Exception {
        assertFalse(invokePrivateIsValidJwtFormat(""));
    }

    @Test
    void getUserFromHeader_WithValidTokenAndUser_ShouldReturnUser() {
        User expectedUser = new User();
        expectedUser.setUsername(VALID_USERNAME);

        when(jwtTokenService.extractUsername(VALID_JWT)).thenReturn(VALID_USERNAME);
        when(userService.findByUsername(VALID_USERNAME)).thenReturn(expectedUser);
        User result = baseController.getUserFromHeader("Bearer " + VALID_JWT);
        assertEquals(expectedUser, result);
        verify(jwtTokenService).extractUsername(VALID_JWT);
        verify(userService).findByUsername(VALID_USERNAME);
    }

    @Test
    void getUserFromHeader_WhenTokenExtractionFails_ShouldThrowTokenInvalidException() {
        String errorMessage = "Token extraction failed";

        when(jwtTokenService.extractUsername(VALID_JWT))
                .thenThrow(new RuntimeException(errorMessage));
        Exception exception = assertThrows(TokenInvalidException.class,
                () -> baseController.getUserFromHeader("Bearer " + VALID_JWT));
        assertTrue(exception.getMessage().contains(errorMessage));
        verify(userService, never()).findByUsername(any());
    }

    @Test
    void getUserFromHeader_WhenUserServiceFails_ShouldThrowTokenInvalidException() {
        String errorMessage = "Database error";

        when(jwtTokenService.extractUsername(VALID_JWT)).thenReturn(VALID_USERNAME);
        when(userService.findByUsername(VALID_USERNAME))
                .thenThrow(new RuntimeException(errorMessage));

        Exception exception = assertThrows(TokenInvalidException.class,
                () -> baseController.getUserFromHeader("Bearer " + VALID_JWT));

        assertTrue(exception.getMessage().contains(errorMessage));
    }

    @Test
    void getUserFromHeader_WithInvalidJwtFormat_ShouldThrowException() {
        String invalidJwt = "invalid.jwt.format";
        assertThrows(TokenInvalidException.class,
                () -> baseController.getUserFromHeader("Bearer " + invalidJwt));
    }

    @Test
    void getUserFromHeader_WithNullHeader_ShouldThrowException() {
        assertThrows(InvalidAuthorizationHeaderException.class,
                () -> baseController.getUserFromHeader(null));
    }

    @Test
    void getUserFromHeader_WithInvalidHeader_ShouldThrowException() {
        assertThrows(InvalidAuthorizationHeaderException.class,
                () -> baseController.getUserFromHeader("InvalidHeader"));
    }
    @Test
    void extractToken_WithEmptyBearer_ShouldThrowException() {
        assertThrows(InvalidAuthorizationHeaderException.class,
                () -> baseController.extractToken("Bearer"));
    }

    @Test
    void getUserFromHeader_WithInvalidJwtFormat_ShouldThrowTokenInvalidException() {
        String invalidJwt = "invalid.jwt.format";

        assertThrows(TokenInvalidException.class,
                () -> baseController.getUserFromHeader("Bearer " + invalidJwt));
    }

    @Test
    void getUserFromHeader_WithValidJwtFormat_ShouldContinueProcessing() {
        String validJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        String username = "testuser";
        User expectedUser = new User();
        expectedUser.setUsername(username);

        when(jwtTokenService.extractUsername(validJwt)).thenReturn(username);
        when(userService.findByUsername(username)).thenReturn(expectedUser);
        assertDoesNotThrow(() -> baseController.getUserFromHeader("Bearer " + validJwt));
        User result = baseController.getUserFromHeader("Bearer " + validJwt);
        assertEquals(expectedUser, result);
    }

    private boolean invokePrivateIsValidJwtFormat(String token) throws Exception {
        Method method = BaseController.class.getDeclaredMethod("isValidJwtFormat", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(baseController, token);
    }
}