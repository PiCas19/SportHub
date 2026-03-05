package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user who gave kudos on Strava.
 * Contains basic display information about the kudoser.
 */
@Data
@NoArgsConstructor
public class Kudoser {
    private String destinationUrl;
    private String displayName;
    private String avatarUrl;
    private boolean showName;
}