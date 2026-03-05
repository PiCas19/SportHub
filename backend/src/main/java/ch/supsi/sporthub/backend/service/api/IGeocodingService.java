package ch.supsi.sporthub.backend.service.api;

/**
 * Service interface for geocoding and geographical computations.
 * Provides methods to convert location names to coordinates, reverse geocode coordinates,
 * and calculate distances between points and line segments.
 */
public interface IGeocodingService {

    /**
     * Converts a location name (e.g., a city or address) into geographical coordinates.
     *
     * @param locationName the name of the location to geocode
     * @return an array containing latitude and longitude [lat, lon]
     */
    double[] geocode(String locationName);

    /**
     * Converts geographical coordinates into a human-readable address or location name.
     *
     * @param lat the latitude of the location
     * @param lon the longitude of the location
     * @return a human-readable representation of the location
     */
    String reverseGeocode(double lat, double lon);

    /**
     * Calculates the shortest distance between a point and a line segment in 2D space.
     *
     * @param px the x-coordinate (longitude) of the point
     * @param py the y-coordinate (latitude) of the point
     * @param ax the x-coordinate of the segment's start point
     * @param ay the y-coordinate of the segment's start point
     * @param bx the x-coordinate of the segment's end point
     * @param by the y-coordinate of the segment's end point
     * @return the shortest distance from the point to the segment
     */
    double distancePointToSegment(double px, double py, double ax, double ay, double bx, double by);
}