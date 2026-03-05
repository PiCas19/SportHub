package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.service.api.IGeocodingService;
import ch.supsi.sporthub.backend.service.api.IBaseApiService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the IGeocodingService interface that provides geocoding and reverse geocoding services.
 * Uses the Nominatim API from OpenStreetMap to convert location names into geographic coordinates and vice versa.
 */
@Service
public class GeocodingServiceImpl implements IGeocodingService {

    private final IBaseApiService baseApiService;
    private final ObjectMapper objectMapper;
    private final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org";

    /**
     * Constructor for the GeocodingServiceImpl class.
     * Initializes the service with the base API service and object mapper.
     *
     * @param baseApiService The service for making HTTP requests to external APIs.
     * @param objectMapper   The object mapper used for JSON processing.
     */
    public GeocodingServiceImpl(IBaseApiService baseApiService, ObjectMapper objectMapper) {
        this.baseApiService = baseApiService;
        this.objectMapper = objectMapper;
    }

    /**
     * Converts a location name (e.g., city, address) to geographical coordinates (latitude, longitude).
     * Uses the Nominatim API to get the coordinates of the location.
     *
     * @param locationName The name of the location to geocode.
     * @return A double array containing the latitude and longitude of the location, or null if not found.
     */
    @Override
    public double[] geocode(String locationName) {
        try {
            String encodedLocation = URLEncoder.encode(locationName, StandardCharsets.UTF_8);
            String endpoint = "/search?q=" + encodedLocation + "&format=json&limit=1";
            ResponseEntity<String> response = baseApiService.get(NOMINATIM_BASE_URL, endpoint, String.class);
            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                return null;
            }
            List<Map<String, Object>> results = objectMapper.readValue(body, new TypeReference<>() {});
            if (results.isEmpty()) {
                return null;
            }
            Map<String, Object> firstResult = results.get(0);
            double lat = Double.parseDouble((String) firstResult.get("lat"));
            double lon = Double.parseDouble((String) firstResult.get("lon"));
            return new double[]{lat, lon};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts geographic coordinates (latitude, longitude) into a human-readable address.
     * Uses the Nominatim API for reverse geocoding.
     *
     * @param lat The latitude of the point to reverse geocode.
     * @param lon The longitude of the point to reverse geocode.
     * @return A string containing the address corresponding to the provided coordinates, or an empty string if not found.
     */
    @Override
    public String reverseGeocode(double lat, double lon) {
        try {
            String endpoint = "/reverse?format=json&lat=" + lat + "&lon=" + lon;
            ResponseEntity<String> response = baseApiService.get(NOMINATIM_BASE_URL, endpoint, String.class);
            String body = response.getBody();
            if (body == null || body.isEmpty()) {
                return "";
            }
            Map<String, Object> result = objectMapper.readValue(body, new TypeReference<>() {});
            return result.get("display_name").toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * Calculates the shortest distance from a point (px, py) to a line segment defined by two points (ax, ay) and (bx, by).
     * The distance is calculated using the Haversine formula.
     *
     * @param px The x-coordinate (longitude) of the point.
     * @param py The y-coordinate (latitude) of the point.
     * @param ax The x-coordinate (longitude) of the first endpoint of the line segment.
     * @param ay The y-coordinate (latitude) of the first endpoint of the line segment.
     * @param bx The x-coordinate (longitude) of the second endpoint of the line segment.
     * @param by The y-coordinate (latitude) of the second endpoint of the line segment.
     * @return The shortest distance from the point to the segment in kilometers.
     */
    @Override
    public double distancePointToSegment(double px, double py,
                                         double ax, double ay,
                                         double bx, double by) {
        double abDistance = haversineDistance(ax, ay, bx, by);
        if (abDistance == 0) {
            return haversineDistance(px, py, ax, ay);
        }
        double t = ((px - ax) * (bx - ax) + (py - ay) * (by - ay)) /
                (Math.pow(bx - ax, 2) + Math.pow(by - ay, 2));
        if (t < 0) {
            return haversineDistance(px, py, ax, ay);
        } else if (t > 1) {
            return haversineDistance(px, py, bx, by);
        } else {
            double projX = ax + t * (bx - ax);
            double projY = ay + t * (by - ay);
            return haversineDistance(px, py, projX, projY);
        }
    }

    /**
     * Calculates the Haversine distance between two points specified by their latitude and longitude.
     *
     * @param lat1 The latitude of the first point.
     * @param lon1 The longitude of the first point.
     * @param lat2 The latitude of the second point.
     * @param lon2 The longitude of the second point.
     * @return The distance between the two points in kilometers.
     */
    private double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}