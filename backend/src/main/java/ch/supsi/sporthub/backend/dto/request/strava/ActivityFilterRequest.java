package ch.supsi.sporthub.backend.dto.request.strava;

import ch.supsi.sporthub.backend.dto.request.Request;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ActivityFilterRequest is a data transfer object (DTO) used to filter Strava activities
 * based on various criteria such as sport type, date range, distance, calories, and keywords.
 * <p>
 * This request is typically used by endpoints that allow querying a user's activity history
 * with filtering options.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActivityFilterRequest extends Request {
    private String sportType;
    private String fromDate;
    private String toDate;
    private Double minDistance;
    private Double maxDistance;
    private String keywords;
    private Double minCalories;
    private Double maxCalories;
}