package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a lap within a Strava activity.
 * Contains detailed metrics about a specific segment of the activity.
 */
@Data
@NoArgsConstructor
public class Lap {
    private long id;
    private String name;
    private Athlete athlete;
    private double distance;
    private int elapsedTime;
    private int movingTime;
    private String startDate;
    private String startDateLocal;
    private double totalElevationGain;
    private double averageSpeed;
    private double maxSpeed;
    private double averageCadence;
    private double averageWatts;
    private int lapIndex;
}