package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents metadata about photos associated with a Strava activity.
 * Contains information about the number of photos and the primary photo selection.
 */
@Data
@NoArgsConstructor
public class PhotoData {
    private boolean usePrimaryPhoto;
    private int count;
    private PrimaryPhoto primary;
}