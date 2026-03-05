package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents a metric for a split in an athlete's activity.
 * Contains data about the distance, time, and performance during a specific split.
 */
@Data
@NoArgsConstructor
public class SplitMetric {
    private double distance;
    private int elapsedTime;
    private double elevationDifference;
    private int movingTime;
    private int split;
    private double averageSpeed;
    private int paceZone;
}