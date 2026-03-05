package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with Strava activity data.
 * Provides methods to create and manage Strava activity objects.
 */
public class StravaActivityUtils {

    /**
     * Creates a list of demo Strava activities for testing or demonstration purposes.
     * The list contains predefined activities with different sport types and distances.
     *
     * @return A list of {@link StravaActivity} objects representing demo activities.
     */
    public static List<StravaActivity> createDemoActivities() {
        List<StravaActivity> activities = new ArrayList<>();
        StravaActivity activity1 = new StravaActivity();
        activity1.setDistanceMeters(15000);
        activity1.setSportType("run");
        activities.add(activity1);
        StravaActivity activity2 = new StravaActivity();
        activity2.setDistanceMeters(25000);
        activity2.setSportType("ride");
        activities.add(activity2);
        StravaActivity activity3 = new StravaActivity();
        activity3.setDistanceMeters(20000);
        activity3.setSportType("run");
        activities.add(activity3);
        StravaActivity activity4 = new StravaActivity();
        activity4.setDistanceMeters(65000);
        activity4.setSportType("run");
        activities.add(activity4);

        return activities;
    }
}