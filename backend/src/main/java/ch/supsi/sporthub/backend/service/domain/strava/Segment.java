package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a Strava segment, which is a specific section of a route or trail
 * that athletes can compete on and track performance.
 */
@Data
@NoArgsConstructor
public class Segment {
    private long id;
    private String name;
    private String activityType;
    private double distance;
    private double averageGrade;
    private double maximumGrade;
    private double elevationHigh;
    private double elevationLow;
    private List<Double> startLatlng;
    private List<Double> endLatlng;
    private String city;
    private String state;
    private String country;
}