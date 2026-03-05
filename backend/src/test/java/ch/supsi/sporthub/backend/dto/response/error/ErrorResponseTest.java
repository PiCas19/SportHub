package ch.supsi.sporthub.backend.dto.response.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorResponseTest {

    @Test
    void testConstructorAndGetters() {
        ErrorResponse response = new ErrorResponse("Not Found", 404);

        assertEquals("Not Found", response.getMessage());
        assertEquals(404, response.getStatusCode());
    }

    @Test
    void testSetters() {
        ErrorResponse response = new ErrorResponse();
        response.setMessage("Unauthorized");
        response.setStatusCode(401);

        assertEquals("Unauthorized", response.getMessage());
        assertEquals(401, response.getStatusCode());
    }

    @Test
    void testEqualsAndHashCode() {
        ErrorResponse r1 = new ErrorResponse("Bad Request", 400);
        ErrorResponse r2 = new ErrorResponse("Bad Request", 400);
        ErrorResponse r3 = new ErrorResponse("Internal Error", 500);

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());

        assertNotEquals(r1, r3);
        assertNotEquals(r1.hashCode(), r3.hashCode());
    }
}
