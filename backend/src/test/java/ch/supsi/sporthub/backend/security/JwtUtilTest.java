package ch.supsi.sporthub.backend.security;

import ch.supsi.sporthub.backend.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JwtUtilTest {

    private JwtProperties jwtProperties;
    private JwtUtil jwtUtil;
    private UserDetails testUser;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("0123456789abcdef0123456789abcdef");
        jwtProperties.setExpirationTime(60_000L);
        jwtProperties.setRefreshExpirationTime(120_000L);
        jwtProperties.setResetExpirationTime(60_000L);
        jwtProperties.setActivationExpirationTime(60_000L);

        jwtUtil = new JwtUtil(jwtProperties);
        testUser = new User("testUser", "password", Collections.emptyList());
    }


    @Test
    void testGenerateAndExtractUsername() {
        String accessToken = jwtUtil.generateToken(testUser);
        String refreshToken = jwtUtil.generateRefreshToken(testUser);

        assertThat(accessToken).isNotEmpty();
        assertThat(refreshToken).isNotEmpty();

        assertThat(jwtUtil.extractUsername(accessToken)).isEqualTo("testUser");
        assertThat(jwtUtil.extractUsername(refreshToken)).isEqualTo("testUser");
    }

    @Test
    void testGetTokenPurpose() {
        String access  = jwtUtil.generateToken(testUser);
        String refresh = jwtUtil.generateRefreshToken(testUser);

        assertThat(jwtUtil.getTokenPurpose(access )).isEqualTo("ACCESS");
        assertThat(jwtUtil.getTokenPurpose(refresh)).isEqualTo("REFRESH");
    }

    @Test
    void testExtractExpirationAndIsTokenExpired_Future() {
        String token = jwtUtil.generateToken(testUser);

        Date expiration = jwtUtil.extractExpiration(token);
        assertThat(expiration).isAfter(new Date());
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    void testExtractExpirationAndIsTokenExpired_Expired() {
        jwtProperties.setExpirationTime(-1);
        jwtUtil = new JwtUtil(jwtProperties);

        String token = jwtUtil.generateToken(testUser);
        assertThatThrownBy(() -> jwtUtil.extractExpiration(token))
                .isInstanceOf(ExpiredJwtException.class);

        assertThatThrownBy(() -> jwtUtil.isTokenExpired(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
    @Test
    void testValidatePasswordResetToken_ValidPurposeNotExpired() {
        String token = jwtUtil.generatePasswordResetToken(testUser);
        assertThat(jwtUtil.validatePasswordResetToken(token)).isTrue();
    }

    @Test
    void testValidatePasswordResetToken_WrongPurpose() {
        String token = jwtUtil.generateActivationToken(testUser);
        assertThat(jwtUtil.validatePasswordResetToken(token)).isFalse();
    }

    @Test
    void testValidatePasswordResetToken_Expired() {
        jwtProperties.setResetExpirationTime(-1);
        jwtUtil = new JwtUtil(jwtProperties);
        String token = jwtUtil.generatePasswordResetToken(testUser);

        assertThatThrownBy(() -> jwtUtil.validatePasswordResetToken(token))
                .isInstanceOf(ExpiredJwtException.class)
                .hasMessageContaining("JWT token has expired");
    }


    @Test
    void testValidateActivationToken_ValidPurposeNotExpired() {
        String token = jwtUtil.generateActivationToken(testUser);
        assertThat(jwtUtil.validateActivationToken(token)).isTrue();
    }

    @Test
    void testValidateActivationToken_WrongPurpose() {
        String token = jwtUtil.generatePasswordResetToken(testUser);
        assertThat(jwtUtil.validateActivationToken(token)).isFalse();
    }

    @Test
    void testValidateActivationToken_Expired() {
        jwtProperties.setActivationExpirationTime(-1);
        jwtUtil = new JwtUtil(jwtProperties);
        String token = jwtUtil.generateActivationToken(testUser);

        assertThatThrownBy(() -> jwtUtil.validateActivationToken(token))
                .isInstanceOf(ExpiredJwtException.class)
                .hasMessageContaining("JWT token has expired");
    }

    @Test
    void testValidateToken_SuccessAndUsernameMismatch() {
        String token = jwtUtil.generateToken(testUser);

        assertThat(jwtUtil.validateToken(token, testUser)).isTrue();

        UserDetails other = new User("other", "pwd", Collections.emptyList());
        assertThat(jwtUtil.validateToken(token, other)).isFalse();
    }

    @Test
    void testExtractUsername_MalformedJwt() {
        assertThatThrownBy(() -> jwtUtil.extractUsername("not.a.jwt"))
                .isInstanceOf(MalformedJwtException.class)
                .hasMessageContaining("Invalid JWT token");
    }

    @Test
    void testExtractUsername_UnsupportedJwt() {
        String header  = java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"alg\":\"RS256\"}".getBytes());
        String payload = java.util.Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"sub\":\"testUser\"}".getBytes());
        String badJwt  = header + "." + payload + ".signature";

        assertThatThrownBy(() -> jwtUtil.extractUsername(badJwt))
                .isInstanceOf(UnsupportedJwtException.class)
                .hasMessage("Unsupported JWT token");
    }

}
