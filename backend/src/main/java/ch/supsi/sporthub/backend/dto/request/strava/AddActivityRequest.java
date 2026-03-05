package ch.supsi.sporthub.backend.dto.request.strava;

import ch.supsi.sporthub.backend.dto.request.Request;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * AddActivityRequest is a data transfer object (DTO) used to create a new activity
 * in the system. It encapsulates all the details needed to define a Strava-like activity,
 * including metadata such as name, type, start date, and physical measurements.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AddActivityRequest extends Request {
    private String name;
    private String type;
    private String sportType;
    private String startDateLocal;
    private int elapsedTime;
    private String description;
    private float distance;
    private Integer trainer;
    private Integer commute;
    private double calories;

    /**
     * Constructor annotated with {@link JsonCreator} to support deserialization from JSON.
     *
     * @param name           Name of the activity.
     * @param type           General type of the activity.
     * @param sportType      Specific sport type.
     * @param startDateLocal Start date and time of the activity (local time).
     * @param elapsedTime    Duration in seconds.
     * @param description    Description of the activity.
     * @param distance       Distance in meters.
     * @param trainer        Flag indicating use of trainer.
     * @param commute        Flag indicating if the activity was a commute.
     */
    @JsonCreator
    public AddActivityRequest(
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("sportType") String sportType,
            @JsonProperty("startDateLocal") String startDateLocal,
            @JsonProperty("elapsedTime") int elapsedTime,
            @JsonProperty("description") String description,
            @JsonProperty("distance") float distance,
            @JsonProperty("trainer") Integer trainer,
            @JsonProperty("commute") Integer commute
    ) {
        this.name = name;
        this.type = type;
        this.sportType = sportType;
        this.startDateLocal = startDateLocal;
        this.elapsedTime = elapsedTime;
        this.description = description;
        this.distance = distance;
        this.trainer = trainer;
        this.commute = commute;
    }
}