package ch.supsi.sporthub.backend.service.domain.strava;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a Strava athlete's profile with personal, social, and equipment information.
 * This model is typically populated from the Strava API.
 */
@Data
@NoArgsConstructor
public class Athlete {
    private long id;
    private String username;
    private String firstname;
    private String lastname;
    private String city;
    private String state;
    private String country;
    private String sex;

    @JsonProperty("profile_medium")
    private String profileMedium;

    private String profile;

    private boolean premium;
    private String createdAt;
    private String updatedAt;
    private int badgeTypeId;

    private Integer followerCount;
    private Integer friendCount;
    private Integer mutualFriendCount;

    @JsonProperty("athlete_type")
    private int athleteType;

    @JsonProperty("date_preference")
    private String datePreference;

    @JsonProperty("measurement_preference")
    private String measurementPreference;

    private List<Object> clubs;
    private Integer ftp;
    private Integer weight;

    private List<Bike> bikes;
    private List<Shoe> shoes;
}