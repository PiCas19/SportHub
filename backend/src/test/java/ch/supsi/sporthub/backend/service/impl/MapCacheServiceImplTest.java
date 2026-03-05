package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.dto.response.MapDataResponse;
import ch.supsi.sporthub.backend.service.domain.strava.ActivitySegment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapCacheServiceImplTest {

    @InjectMocks
    private MapCacheServiceImpl mapCacheService;

    private MapDataResponse testData;
    private List<ActivitySegment> testSegments;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ActivitySegment segment1 = new ActivitySegment(1.0, 2.0, 3.0, 4.0, "Segment 1");
        segment1.addPoint(1.1, 2.1);
        segment1.addPoint(1.2, 2.2);
        ActivitySegment segment2 = new ActivitySegment(5.0, 6.0, 7.0, 8.0, "Segment 2");
        segment2.addPoint(5.1, 6.1);
        testSegments = Arrays.asList(segment1, segment2);
        testData = new MapDataResponse(46.0, 9.0, testSegments);
    }

    @Test
    void testSaveMapData() {
        String token = mapCacheService.saveMapData(testData);
        assertNotNull(token);
        assertEquals(8, token.length());
    }

    @Test
    void testGetMapData_exists() {
        String token = mapCacheService.saveMapData(testData);
        MapDataResponse retrievedData = mapCacheService.getMapData(token);
        assertNotNull(retrievedData);
        assertEquals(testData.getTargetLat(), retrievedData.getTargetLat());
        assertEquals(testData.getTargetLon(), retrievedData.getTargetLon());
        assertEquals(testData.getSegments().size(), retrievedData.getSegments().size());
        for (int i = 0; i < testData.getSegments().size(); i++) {
            ActivitySegment originalSegment = testData.getSegments().get(i);
            ActivitySegment retrievedSegment = retrievedData.getSegments().get(i);
            assertEquals(originalSegment.getStartLat(), retrievedSegment.getStartLat());
            assertEquals(originalSegment.getStartLon(), retrievedSegment.getStartLon());
            assertEquals(originalSegment.getEndLat(), retrievedSegment.getEndLat());
            assertEquals(originalSegment.getEndLon(), retrievedSegment.getEndLon());
            assertEquals(originalSegment.getLabel(), retrievedSegment.getLabel());
            assertEquals(originalSegment.getPolyline().size(), retrievedSegment.getPolyline().size());
            for (int j = 0; j < originalSegment.getPolyline().size(); j++) {
                assertArrayEquals(originalSegment.getPolyline().get(j), retrievedSegment.getPolyline().get(j));
            }
        }
    }

    @Test
    void testGetMapData_notExists() {
        MapDataResponse retrievedData = mapCacheService.getMapData("nonExistentToken");
        assertNull(retrievedData);
    }
}