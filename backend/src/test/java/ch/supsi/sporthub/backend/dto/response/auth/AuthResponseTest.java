package ch.supsi.sporthub.backend.dto.response.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthResponseTest {

    @Test
    void testConstructorAndGetters() {
        AuthResponse response = new AuthResponse("Success", "access123", "refresh456");

        assertEquals("Success", response.getMessage());
        assertEquals("access123", response.getAccessToken());
        assertEquals("refresh456", response.getRefreshToken());
    }

    @Test
    void testSetters() {
        AuthResponse response = new AuthResponse();
        response.setMessage("Updated");
        response.setAccessToken("newAccess");
        response.setRefreshToken("newRefresh");

        assertEquals("Updated", response.getMessage());
        assertEquals("newAccess", response.getAccessToken());
        assertEquals("newRefresh", response.getRefreshToken());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthResponse r1 = new AuthResponse("OK", "token1", "refresh1");
        AuthResponse r2 = new AuthResponse("OK", "token1", "refresh1");
        AuthResponse r3 = new AuthResponse("FAIL", "token2", "refresh2");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}
