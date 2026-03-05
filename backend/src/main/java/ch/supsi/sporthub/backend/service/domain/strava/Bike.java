package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a bicycle associated with a Strava athlete.
 * Contains identification and usage information as returned by the Strava API.
 */
@Data
@NoArgsConstructor
public class Bike {
    private String id;
    private boolean primary;
    private String name;
    private int resourceState;
    private double distance;
}