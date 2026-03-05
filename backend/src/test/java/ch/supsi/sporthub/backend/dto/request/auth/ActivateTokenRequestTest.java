package ch.supsi.sporthub.backend.dto.request.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActivateTokenRequestTest {

    @Test
    void testConstructorAndGetter() {
        ActivateTokenRequest request = new ActivateTokenRequest("abc123");
        assertEquals("abc123", request.getToken());
    }

    @Test
    void testEqualsAndHashCode() {
        ActivateTokenRequest req1 = new ActivateTokenRequest("token123");
        ActivateTokenRequest req2 = new ActivateTokenRequest("token123");
        ActivateTokenRequest req3 = new ActivateTokenRequest("different");

        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());

        assertNotEquals(req1, req3);
        assertNotEquals(req1.hashCode(), req3.hashCode());
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{\"token\": \"abc123\"}";
        ObjectMapper mapper = new ObjectMapper();

        ActivateTokenRequest request = mapper.readValue(json, ActivateTokenRequest.class);

        assertEquals("abc123", request.getToken());
    }
}
