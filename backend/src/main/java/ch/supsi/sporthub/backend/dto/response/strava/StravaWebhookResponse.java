package ch.supsi.sporthub.backend.dto.response.strava;

import ch.supsi.sporthub.backend.dto.response.Response;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents the response to a webhook event triggered by Strava.
 * <p>
 * This class extends {@link Response} and provides additional details
 * about whether the notification was successfully processed and
 * includes the name of the associated activity.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class StravaWebhookResponse extends Response {
    private boolean notified;
    private String activityName;

    /**
     * Constructs a new StravaWebhookResponse with the specified message,
     * notification status, and activity name.
     *
     * @param message       the response message
     * @param notified      whether the webhook was successfully handled
     * @param activityName  the name of the activity involved in the event
     */
    public StravaWebhookResponse(String message, boolean notified, String activityName) {
        super(message);
        this.notified = notified;
        this.activityName = activityName;
    }
}