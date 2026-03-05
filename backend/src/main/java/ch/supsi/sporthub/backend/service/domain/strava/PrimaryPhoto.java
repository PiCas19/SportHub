package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Represents the primary photo associated with a Strava activity.
 * Contains a unique identifier and a map of image URLs in various sizes.
 */
@Data
@NoArgsConstructor
public class PrimaryPhoto {
    private String uniqueId;
    private Map<String, String> urls;
}