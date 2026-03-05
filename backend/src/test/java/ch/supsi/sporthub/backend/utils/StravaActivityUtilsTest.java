package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.service.domain.strava.StravaActivity;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class StravaActivityUtilsTest {

    @Test
    void testCreateDemoActivities() {
        List<StravaActivity> activities = StravaActivityUtils.createDemoActivities();
        assertEquals(4, activities.size());
        assertEquals(15000, activities.get(0).getDistanceMeters());
        assertEquals("run", activities.get(0).getSportType());

        assertEquals(25000, activities.get(1).getDistanceMeters());
        assertEquals("ride", activities.get(1).getSportType());

        assertEquals(20000, activities.get(2).getDistanceMeters());
        assertEquals("run", activities.get(2).getSportType());

        assertEquals(65000, activities.get(3).getDistanceMeters());
        assertEquals("run", activities.get(3).getSportType());
    }
}
