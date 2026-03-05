package ch.supsi.sporthub.backend.dto.response.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AllTokenResponseTest {

    @Test
    void testConstructorAndGetters() {
        AllTokenResponse response = new AllTokenResponse("Success", true);

        assertEquals("Success", response.getMessage());
        assertTrue(response.isValid());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        AllTokenResponse response = new AllTokenResponse();
        response.setMessage("OK");
        response.setValid(false);

        assertEquals("OK", response.getMessage());
        assertFalse(response.isValid());
    }

    @Test
    void testEqualsAndHashCode() {
        AllTokenResponse r1 = new AllTokenResponse("Same", true);
        AllTokenResponse r2 = new AllTokenResponse("Same", true);
        AllTokenResponse r3 = new AllTokenResponse("Different", false);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}
