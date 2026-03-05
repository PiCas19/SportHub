package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a segment of a Strava activity, including start and end coordinates,
 * a label for the segment, and an optional polyline path.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivitySegment {
    public double startLat;
    public double startLon;
    public double endLat;
    public double endLon;
    public String label;
    @Setter
    @Getter
    private List<double[]> polyline = new ArrayList<>();

    /**
     * Constructs an ActivitySegment with basic attributes (polyline initialized empty).
     *
     * @param startLat  Latitude of the start point
     * @param startLon  Longitude of the start point
     * @param endLat    Latitude of the end point
     * @param endLon    Longitude of the end point
     * @param label     Label describing the segment
     */
    public ActivitySegment(double startLat, double startLon, double endLat, double endLon, String label) {
        this.startLat = startLat;
        this.startLon = startLon;
        this.endLat = endLat;
        this.endLon = endLon;
        this.label = label;
        this.polyline = new ArrayList<>();
    }

    /**
     * Adds a point to the segment’s polyline path.
     *
     * @param lat Latitude of the point
     * @param lon Longitude of the point
     */
    public void addPoint(double lat, double lon) {
        this.polyline.add(new double[]{lat, lon});
    }
}