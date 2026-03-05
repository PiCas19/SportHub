package ch.supsi.sporthub.backend.dto.request.strava;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AuthorizationStravaCodeRequestTest {

    @Test
    void testConstructorAndGetter() {
        AuthorizationStravaCodeRequest request = new AuthorizationStravaCodeRequest("abc123");
        assertEquals("abc123", request.getCode());
    }

    @Test
    void testSetter() {
        AuthorizationStravaCodeRequest request = new AuthorizationStravaCodeRequest();
        request.setCode("newCode");
        assertEquals("newCode", request.getCode());
    }

    @Test
    void testEqualsAndHashCode() {
        AuthorizationStravaCodeRequest req1 = new AuthorizationStravaCodeRequest("code123");
        AuthorizationStravaCodeRequest req2 = new AuthorizationStravaCodeRequest("code123");
        AuthorizationStravaCodeRequest req3 = new AuthorizationStravaCodeRequest("different");

        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());
        assertNotEquals(req1, req3);
    }

    @Test
    void testJsonDeserialization() throws Exception {
        String json = "{\"code\": \"abc123\"}";
        ObjectMapper mapper = new ObjectMapper();
        AuthorizationStravaCodeRequest request = mapper.readValue(json, AuthorizationStravaCodeRequest.class);

        assertEquals("abc123", request.getCode());
    }
}
