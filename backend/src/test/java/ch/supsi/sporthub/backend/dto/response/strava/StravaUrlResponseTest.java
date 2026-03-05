package ch.supsi.sporthub.backend.dto.response.strava;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StravaUrlResponseTest {

    @Test
    void testConstructorAndGetters() {
        StravaUrlResponse response = new StravaUrlResponse("Success", "https://strava.com/auth");

        assertEquals("Success", response.getMessage());
        assertEquals("https://strava.com/auth", response.getAuthorizationUrl());
    }

    @Test
    void testSetters() {
        StravaUrlResponse response = new StravaUrlResponse("init", "url");
        response.setMessage("Updated");
        response.setAuthorizationUrl("https://new.url");

        assertEquals("Updated", response.getMessage());
        assertEquals("https://new.url", response.getAuthorizationUrl());
    }

    @Test
    void testEqualsAndHashCode() {
        StravaUrlResponse r1 = new StravaUrlResponse("OK", "https://strava.com/a");
        StravaUrlResponse r2 = new StravaUrlResponse("OK", "https://strava.com/a");
        StravaUrlResponse r3 = new StravaUrlResponse("Fail", "https://strava.com/b");

        assertEquals(r1, r2);
        assertEquals(r1.hashCode(), r2.hashCode());
        assertNotEquals(r1, r3);
    }
}
