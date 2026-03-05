package ch.supsi.sporthub.backend.dto.request.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TokenRequestTest {
    static class TestTokenRequest extends TokenRequest {
        public TestTokenRequest(String token) {
            super(token);
        }
    }

    @Test
    void testConstructorAndGetter() {
        TestTokenRequest request = new TestTokenRequest("token123");
        assertEquals("token123", request.getToken());
    }

    @Test
    void testEqualsAndHashCode() {
        TestTokenRequest req1 = new TestTokenRequest("token123");
        TestTokenRequest req2 = new TestTokenRequest("token123");
        TestTokenRequest req3 = new TestTokenRequest("other");

        assertEquals(req1, req2);
        assertEquals(req1.hashCode(), req2.hashCode());

        assertNotEquals(req1, req3);
        assertNotEquals(req1.hashCode(), req3.hashCode());
    }
}
