package ch.supsi.sporthub.backend.service.domain.strava;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a running shoe associated with a Strava athlete.
 * Includes details about the shoe's identity, usage, and status.
 */
@Data
@NoArgsConstructor
public class Shoe {
    private String id;
    private boolean primary;
    private String name;
    private int resourceState;
    private double distance;
}