package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Represents an athlete's effort on a specific Strava segment.
 * Contains performance data and references to the segment and athlete.
 */
@Data
@NoArgsConstructor
public class SegmentEffort {
    private long id;
    private String name;
    private Athlete athlete;
    private double distance;
    private int elapsedTime;
    private int movingTime;
    private String startDate;
    private String startDateLocal;
    private Segment segment;
}