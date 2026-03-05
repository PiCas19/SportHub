package ch.supsi.sporthub.backend.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to decode a polyline encoded in Google's polyline format.
 * The polyline encoding is often used to represent a series of latitude/longitude points as a string.
 */
public class PolylineUtils {

    /**
     * Decodes a polyline string into a list of geographical coordinates.
     * The polyline string is encoded in a compact form, where each coordinate difference is stored using a series of 5-bit values.
     * The method will decode the polyline into a list of latitude/longitude points.
     *
     * @param polyline The encoded polyline string.
     * @return A list of geographical coordinates, each represented as a List of two Doubles:
     *         the first element is latitude, and the second is longitude.
     */
    public static List<List<Double>> decodePolyline(String polyline) {
        List<List<Double>> poly = new ArrayList<>();
        int index = 0;
        int len = polyline.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                if (index >= len) break;
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                if (index >= len) break;
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            List<Double> point = new ArrayList<>(2);
            point.add(lat / 1E5);
            point.add(lng / 1E5);
            poly.add(point);
            if (index >= len) break;
        }
        return poly;
    }
}