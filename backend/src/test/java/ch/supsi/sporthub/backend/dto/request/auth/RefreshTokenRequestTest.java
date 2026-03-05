package ch.supsi.sporthub.backend.dto.request.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RefreshTokenRequestTest {

    @Test
    void testConstructorAndGetters() {
        RefreshTokenRequest request = new RefreshTokenRequest("abc123", "john_doe");
        assertEquals("abc123", request.getToken());
        assertEquals("john_doe", request.getUsername());
    }

    @Test
    void testEqualsAndHashCode() {
        RefreshTokenRequest req1 = new RefreshTokenRequest("token123", "user1");
        RefreshTokenRequest req2 = new RefreshTokenRequest("token123", "user1");
        RefreshTokenRequest req3 = new RefreshTokenRequest("token456", "user2");

        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());

        assertNotEquals(req1, req3);
        assertNotEquals(req1.hashCode(), req3.hashCode());
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{\"token\": \"abc123\", \"username\": \"john_doe\"}";
        ObjectMapper mapper = new ObjectMapper();

        RefreshTokenRequest request = mapper.readValue(json, RefreshTokenRequest.class);

        assertEquals("abc123", request.getToken());
        assertEquals("john_doe", request.getUsername());
    }
}
