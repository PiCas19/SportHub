package ch.supsi.sporthub.backend.dto.response;

import ch.supsi.sporthub.backend.service.domain.strava.ActivitySegment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * Response class that provides map-related data for visualizing an activity.
 * <p>
 * Includes the target coordinates and a list of route segments associated with the activity.
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class MapDataResponse extends Response {
    private double targetLat;
    private double targetLon;
    private List<ActivitySegment> segments;

}