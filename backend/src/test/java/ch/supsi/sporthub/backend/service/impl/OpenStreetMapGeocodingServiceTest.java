package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import ch.supsi.sporthub.backend.service.domain.strava.ActivitySegment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OpenStreetMapGeocodingServiceTest {

    private IBaseApiService baseApiService;
    private ObjectMapper objectMapper;
    private GeocodingServiceImpl service;

    @BeforeEach
    void setUp() {
        baseApiService = mock(IBaseApiService.class);
        objectMapper = mock(ObjectMapper.class);
        service = new GeocodingServiceImpl(baseApiService, objectMapper);
    }

    @Test
    void testGeocode_ReturnsCoordinates() throws Exception {
        String json = "[{\"lat\":\"46.0\", \"lon\":\"8.0\"}]";
        ResponseEntity<String> response = ResponseEntity.ok(json);
        when(baseApiService.get(anyString(), anyString(), eq(String.class))).thenReturn(response);
        when(objectMapper.readValue(eq(json), any(TypeReference.class)))
                .thenReturn(List.of(Map.of("lat", "46.0", "lon", "8.0")));

        double[] result = service.geocode("Lugano");

        assertNotNull(result);
        assertEquals(46.0, result[0]);
        assertEquals(8.0, result[1]);
    }

    @Test
    void testGeocode_EmptyBody() {
        ResponseEntity<String> response = ResponseEntity.ok("");
        when(baseApiService.get(anyString(), anyString(), eq(String.class))).thenReturn(response);

        assertNull(service.geocode("Nowhere"));
    }

    @Test
    void testGeocode_NullBody() {
        ResponseEntity<String> response = ResponseEntity.ok(null);
        when(baseApiService.get(anyString(), anyString(), eq(String.class))).thenReturn(response);

        assertNull(service.geocode("Nowhere"));
    }

    @Test
    void testGeocode_EmptyResults() throws Exception {
        String json = "[]";
        ResponseEntity<String> response = ResponseEntity.ok(json);
        when(baseApiService.get(anyString(), anyString(), eq(String.class))).thenReturn(response);
        when(objectMapper.readValue(eq(json), any(TypeReference.class))).thenReturn(List.of());

        assertNull(service.geocode("Desert"));
    }

    @Test
    void testGeocode_Exception() {
        when(baseApiService.get(anyString(), anyString(), eq(String.class))).thenThrow(new RuntimeException("fail"));

        assertNull(service.geocode("fail"));
    }

    @Test
    void testReverseGeocode_ValidResponse() throws Exception {
        String json = "{\"display_name\": \"Lugano, Switzerland\"}";
        ResponseEntity<String> response = ResponseEntity.ok(json);
        when(baseApiService.get(anyString(), anyString(), eq(String.class))).thenReturn(response);
        when(objectMapper.readValue(eq(json), any(TypeReference.class)))
                .thenReturn(Map.of("display_name", "Lugano, Switzerland"));

        String result = service.reverseGeocode(46.0, 8.0);
        assertEquals("Lugano, Switzerland", result);
    }

    @Test
    void testReverseGeocode_EmptyBody() {
        ResponseEntity<String> response = ResponseEntity.ok("");
        when(baseApiService.get(anyString(), anyString(), eq(String.class))).thenReturn(response);

        String result = service.reverseGeocode(0.0, 0.0);
        assertEquals("", result);
    }

    @Test
    void testReverseGeocode_NullBody() {
        ResponseEntity<String> response = ResponseEntity.ok(null);
        when(baseApiService.get(anyString(), anyString(), eq(String.class))).thenReturn(response);

        String result = service.reverseGeocode(0.0, 0.0);
        assertEquals("", result);
    }

    @Test
    void testReverseGeocode_Exception() {
        when(baseApiService.get(anyString(), anyString(), eq(String.class)))
                .thenThrow(new RuntimeException("boom"));

        String result = service.reverseGeocode(0.0, 0.0);
        assertEquals("", result);
    }

    @Test
    void testDistancePointToSegment_ZeroLengthSegment() {
        double d = service.distancePointToSegment(1, 1, 2, 2, 2, 2);
        assertTrue(d > 0);
    }

    @Test
    void testDistancePointToSegment_BeforeA() {
        double d = service.distancePointToSegment(0, 0, 1, 1, 3, 3);
        assertTrue(d > 0);
    }

    @Test
    void testDistancePointToSegment_AfterB() {
        double d = service.distancePointToSegment(5, 5, 1, 1, 3, 3);
        assertTrue(d > 0);
    }

    @Test
    void testDistancePointToSegment_BetweenAandB() {
        double d = service.distancePointToSegment(2, 2, 1, 1, 3, 3);
        assertEquals(0.0, d, 0.001);
    }

    @Test
    void testGenerateMultiActivityMapUrl() {
        List<ActivitySegment> segments = List.of(
                new ActivitySegment(45.0, 7.0, 45.5, 7.5, "Run 1"),
                new ActivitySegment(46.0, 8.0, 46.5, 8.5, "Run 2")
        );
    }

    @Test
    void testGenerateMultiActivityMapUrl_ExceptionHandling() {
        List<ActivitySegment> segments = mock(List.class);
        when(segments.size()).thenThrow(new RuntimeException("forced error"));
    }

}