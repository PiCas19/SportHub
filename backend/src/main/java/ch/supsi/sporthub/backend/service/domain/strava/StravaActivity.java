package ch.supsi.sporthub.backend.service.domain.strava;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Represents a Strava activity.
 * Contains detailed information about the activity including metrics, metadata, and related objects.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StravaActivity {
    private Long id;
    private String name;

    @JsonProperty("distance")
    private double distanceMeters;

    @JsonProperty("moving_time")
    private int movingTimeSeconds;

    @JsonProperty("elapsed_time")
    private int elapsedTimeSeconds;

    @JsonProperty("total_elevation_gain")
    private double totalElevationGain;

    @JsonProperty("sport_type")
    private String sportType;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("start_date_local")
    private String startDateLocal;

    private String timezone;

    @JsonProperty("utc_offset")
    private int utcOffset;

    @JsonProperty("start_latlng")
    private List<Double> startLatlng;

    @JsonProperty("end_latlng")
    private List<Double> endLatlng;

    private int achievementCount;
    private int kudosCount;
    private int commentCount;
    private int athleteCount;
    private int photoCount;

    private Map<String, Object> map;

    private boolean trainer;
    private boolean commute;
    private boolean manual;
    private boolean privateActivity;
    private boolean flagged;

    @JsonProperty("gear_id")
    private String gearId;

    @JsonProperty("average_speed")
    private double averageSpeed;

    @JsonProperty("max_speed")
    private double maxSpeed;

    @JsonProperty("average_cadence")
    private double averageCadence;

    @JsonProperty("average_watts")
    private double averageWatts;

    @JsonProperty("weighted_average_watts")
    private int weightedAverageWatts;

    @JsonProperty("kilojoules")
    private double kilojoules;

    @JsonProperty("device_watts")
    private boolean deviceWatts;

    @JsonProperty("has_heartrate")
    private boolean hasHeartrate;

    @JsonProperty("max_watts")
    private int maxWatts;

    @JsonProperty("elev_high")
    private double elevHigh;

    @JsonProperty("elev_low")
    private double elevLow;

    @JsonProperty("total_photo_count")
    private int totalPhotoCount;

    private boolean hasKudoed;

    @JsonProperty("workout_type")
    private Integer workoutType;

    @JsonProperty("suffer_score")
    private Integer sufferScore;

    private String description;

    @JsonProperty("calories")
    private double calories;

    @JsonProperty("segment_efforts")
    private List<SegmentEffort> segmentEfforts;

    @JsonProperty("splits_metric")
    private List<SplitMetric> splitsMetric;

    @JsonProperty("laps")
    private List<Lap> laps;

    @JsonProperty("photos")
    private PhotoData photos;

    @JsonProperty("highlighted_kudosers")
    private List<Kudoser> highlightedKudosers;

    @JsonProperty("device_name")
    private String deviceName;

    @JsonProperty("embed_token")
    private String embedToken;
}