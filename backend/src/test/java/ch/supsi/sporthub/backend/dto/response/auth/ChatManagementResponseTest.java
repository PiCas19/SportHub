package ch.supsi.sporthub.backend.dto.response.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatManagementResponseTest {

    @Test
    void testConstructorAndGetters() {
        ChatManagementResponse response = new ChatManagementResponse("Created", true, 201);

        assertEquals("Created", response.getMessage());
        assertTrue(response.isSuccess());
        assertEquals(201, response.getCode());
    }

    @Test
    void testNoArgsConstructorAndSetters() {
        ChatManagementResponse response = new ChatManagementResponse();
        response.setMessage("OK");
        response.setSuccess(false);
        response.setCode(400);

        assertEquals("OK", response.getMessage());
        assertFalse(response.isSuccess());
        assertEquals(400, response.getCode());
    }

    @Test
    void testEqualsAndHashCode() {
        ChatManagementResponse r1 = new ChatManagementResponse("OK", true, 200);
        ChatManagementResponse r2 = new ChatManagementResponse("OK", true, 200);
        ChatManagementResponse r3 = new ChatManagementResponse("Error", false, 500);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}
