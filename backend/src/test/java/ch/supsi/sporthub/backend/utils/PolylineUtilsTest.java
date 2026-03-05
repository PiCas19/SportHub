package ch.supsi.sporthub.backend.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PolylineUtilsTest {

    @Test
    void testDecodePolyline_ValidPolyline() {
        String polyline = "_p~iF~ps|U_ulLnnqC_mqNvxq`@";
        List<List<Double>> result = PolylineUtils.decodePolyline(polyline);

        assertEquals(3, result.size());
        assertEquals(38.5, result.get(0).get(0), 1e-5);
        assertEquals(-120.2, result.get(0).get(1), 1e-5);
        assertEquals(40.7, result.get(1).get(0), 1e-5);
        assertEquals(-120.95, result.get(1).get(1), 1e-5);
        assertEquals(43.252, result.get(2).get(0), 1e-5);
        assertEquals(-126.453, result.get(2).get(1), 1e-5);
    }

    @Test
    void testDecodePolyline_EmptyInput() {
        String polyline = "";
        List<List<Double>> result = PolylineUtils.decodePolyline(polyline);
        assertTrue(result.isEmpty());
    }

    @Test
    void testDecodePolyline_IncompleteEncoding() {
        String polyline = "_p~iF~ps|U_ulL";
        List<List<Double>> result = PolylineUtils.decodePolyline(polyline);
        assertFalse(result.isEmpty());
        assertEquals(2, result.size());
    }
}
