package ch.supsi.sporthub.backend.dto.request.strava;

import ch.supsi.sporthub.backend.dto.request.Request;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * StravaWebhookEvent is a data transfer object (DTO) that represents an incoming webhook event
 * sent by Strava's API when a subscribed resource is updated.
 * <p>
 * This class is used to deserialize the JSON payload received from Strava during webhook callbacks.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StravaWebhookEvent extends Request {
    @JsonProperty("aspect_type")
    private String aspectType;

    @JsonProperty("event_time")
    private Long eventTime;

    @JsonProperty("object_id")
    private Long objectId;

    @JsonProperty("object_type")
    private String objectType;

    @JsonProperty("owner_id")
    private Long ownerId;

    @JsonProperty("subscription_id")
    private Long subscriptionId;

    private Map<String, Object> updates;
}