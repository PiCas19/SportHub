package ch.supsi.sporthub.backend.dto.response.strava;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class StravaWebhookResponseTest {
    @Test
    public void testConstructorAndGetters() {
        String message = "Operazione completata";
        boolean notified = true;
        String activityName = "Corsa";

        StravaWebhookResponse response = new StravaWebhookResponse(message, notified, activityName);
        assertEquals(message, response.getMessage());
        assertEquals(notified, response.isNotified());
        assertEquals(activityName, response.getActivityName());
    }

    @Test
    public void testEqualsAndHashCode() {
        String message = "Operazione completata";
        boolean notified = true;
        String activityName = "Corsa";

        StravaWebhookResponse response1 = new StravaWebhookResponse(message, notified, activityName);
        StravaWebhookResponse response2 = new StravaWebhookResponse(message, notified, activityName);
        assertEquals(response1, response2);
        assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    public void testNotEquals() {
        StravaWebhookResponse response1 = new StravaWebhookResponse("Messaggio 1", true, "Corsa");
        StravaWebhookResponse response2 = new StravaWebhookResponse("Messaggio 2", false, "Ciclismo");
        assertNotEquals(response1, response2);
    }

}
