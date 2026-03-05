package ch.supsi.sporthub.backend.service;

import ch.supsi.sporthub.backend.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtTokenServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetails userDetails;

    private JwtTokenService jwtTokenService;

    @BeforeEach
    public void setup() {
        jwtTokenService = new JwtTokenService(jwtUtil);
    }

    @Test
    public void testGenerateAccessToken() {
        String expectedToken = "access.token.example";
        when(jwtUtil.generateToken(userDetails)).thenReturn(expectedToken);
        String result = jwtTokenService.generateAccessToken(userDetails);
        assertEquals(expectedToken, result);
        verify(jwtUtil).generateToken(userDetails);
    }

    @Test
    public void testGenerateRefreshToken() {
        String expectedToken = "refresh.token.example";
        when(jwtUtil.generateRefreshToken(userDetails)).thenReturn(expectedToken);
        String result = jwtTokenService.generateRefreshToken(userDetails);
        assertEquals(expectedToken, result);
        verify(jwtUtil).generateRefreshToken(userDetails);
    }

    @Test
    public void testGeneratePasswordResetToken() {
        String expectedToken = "reset.token.example";
        when(jwtUtil.generatePasswordResetToken(userDetails)).thenReturn(expectedToken);
        String result = jwtTokenService.generatePasswordResetToken(userDetails);
        assertEquals(expectedToken, result);
        verify(jwtUtil).generatePasswordResetToken(userDetails);
    }

    @Test
    public void testGenerateActivationToken() {
        String expectedToken = "activation.token.example";
        when(jwtUtil.generateActivationToken(userDetails)).thenReturn(expectedToken);
        String result = jwtTokenService.generateActivationToken(userDetails);
        assertEquals(expectedToken, result);
        verify(jwtUtil).generateActivationToken(userDetails);
    }

    @Test
    public void testExtractUsername() {
        String token = "valid.jwt.token";
        String expectedUsername = "testuser";
        when(jwtUtil.extractUsername(token)).thenReturn(expectedUsername);
        String result = jwtTokenService.extractUsername(token);
        assertEquals(expectedUsername, result);
        verify(jwtUtil).extractUsername(token);
    }

    @Test
    public void testValidatePasswordResetToken_Valid() {
        String token = "valid.reset.token";
        when(jwtUtil.validatePasswordResetToken(token)).thenReturn(true);
        boolean result = jwtTokenService.validatePasswordResetToken(token);
        assertTrue(result);
        verify(jwtUtil).validatePasswordResetToken(token);
    }

    @Test
    public void testValidatePasswordResetToken_Invalid() {
        String token = "invalid.reset.token";
        when(jwtUtil.validatePasswordResetToken(token)).thenReturn(false);
        boolean result = jwtTokenService.validatePasswordResetToken(token);
        assertFalse(result);
        verify(jwtUtil).validatePasswordResetToken(token);
    }

    @Test
    public void testValidateActivationToken_Valid() {
        String token = "valid.activation.token";
        when(jwtUtil.validateActivationToken(token)).thenReturn(true);
        boolean result = jwtTokenService.validateActivationToken(token);
        assertTrue(result);
        verify(jwtUtil).validateActivationToken(token);
    }

    @Test
    public void testValidateActivationToken_Invalid() {
        String token = "invalid.activation.token";
        when(jwtUtil.validateActivationToken(token)).thenReturn(false);
        boolean result = jwtTokenService.validateActivationToken(token);
        assertFalse(result);
        verify(jwtUtil).validateActivationToken(token);
    }

    @Test
    public void testIsTokenValid_Valid() {
        String token = "valid.jwt.token";

        when(jwtUtil.getTokenPurpose(token)).thenReturn("ACCESS");
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        boolean result = jwtTokenService.isTokenValid(token, userDetails);

        assertTrue(result);
        verify(jwtUtil).getTokenPurpose(token);
        verify(jwtUtil).validateToken(token, userDetails);
    }

    @Test
    public void testIsTokenValid_Invalid() {
        String token = "invalid.jwt.token";

        when(jwtUtil.getTokenPurpose(token)).thenReturn("ACCESS");
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(false);

        boolean result = jwtTokenService.isTokenValid(token, userDetails);

        assertFalse(result);
        verify(jwtUtil).getTokenPurpose(token);
        verify(jwtUtil).validateToken(token, userDetails);
    }

    @Test
    public void testIsRefreshTokenValid_Valid() {
        String refreshToken = "valid.refresh.token";
        when(jwtUtil.validateToken(refreshToken, userDetails)).thenReturn(true);
        boolean result = jwtTokenService.isRefreshTokenValid(refreshToken, userDetails);
        assertTrue(result);
        verify(jwtUtil).validateToken(refreshToken, userDetails);
    }

    @Test
    public void testIsRefreshTokenValid_Invalid() {
        String refreshToken = "invalid.refresh.token";
        when(jwtUtil.validateToken(refreshToken, userDetails)).thenReturn(false);
        boolean result = jwtTokenService.isRefreshTokenValid(refreshToken, userDetails);
        assertFalse(result);
        verify(jwtUtil).validateToken(refreshToken, userDetails);
    }

    @Test
    public void testGetTokenPurpose() {
        String token = "some.jwt.token";
        String expectedPurpose = "password_reset";
        when(jwtUtil.getTokenPurpose(token)).thenReturn(expectedPurpose);

        String result = jwtTokenService.getTokenPurpose(token);

        assertEquals(expectedPurpose, result);
        verify(jwtUtil).getTokenPurpose(token);
    }

    @Test
    public void testIsTokenValid_UnknownPurpose() {
        String token = "some.unknown.token";

        when(jwtUtil.getTokenPurpose(token)).thenReturn("UNKNOWN");

        boolean result = jwtTokenService.isTokenValid(token, userDetails);

        assertFalse(result);
        verify(jwtUtil).getTokenPurpose(token);
    }

    @Test
    void testIsTokenValid_PasswordReset() {
        String token = "reset.token";
        when(jwtUtil.getTokenPurpose(token)).thenReturn("PASSWORD_RESET");
        when(jwtUtil.validatePasswordResetToken(token)).thenReturn(true);

        boolean result = jwtTokenService.isTokenValid(token, userDetails);
        assertTrue(result);
        verify(jwtUtil).getTokenPurpose(token);
        verify(jwtUtil).validatePasswordResetToken(token);
    }
    @Test
    void testIsTokenValid_Activation() {
        String token = "activation.token";
        when(jwtUtil.getTokenPurpose(token)).thenReturn("ACTIVATION");
        when(jwtUtil.validateActivationToken(token)).thenReturn(true);

        boolean result = jwtTokenService.isTokenValid(token, userDetails);
        assertTrue(result);
        verify(jwtUtil).getTokenPurpose(token);
        verify(jwtUtil).validateActivationToken(token);
    }

    @Test
    void testIsTokenValid_Refresh() {
        String token = "refresh.token.example";

        when(jwtUtil.getTokenPurpose(token)).thenReturn("REFRESH");
        when(jwtUtil.validateToken(token, userDetails)).thenReturn(true);

        boolean result = jwtTokenService.isTokenValid(token, userDetails);

        assertTrue(result);
        verify(jwtUtil).getTokenPurpose(token);
        verify(jwtUtil).validateToken(token, userDetails);
    }

}