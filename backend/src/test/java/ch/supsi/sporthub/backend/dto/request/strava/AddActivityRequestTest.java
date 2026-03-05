package ch.supsi.sporthub.backend.dto.request.strava;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class AddActivityRequestTest {

    @Test
    public void testJsonCreatorConstructorAndGetters() {
        String name = "Morning Run";
        String type = "Run";
        String sportType = "Cardio";
        String startDateLocal = "2025-04-01T07:30:00";
        int elapsedTime = 3600;
        String description = "Corsa mattutina";
        float distance = 10.5f;
        Integer trainer = 1;
        Integer commute = 0;
        AddActivityRequest request = new AddActivityRequest(name, type, sportType, startDateLocal, elapsedTime, description, distance, trainer, commute);
        assertEquals(name, request.getName());
        assertEquals(type, request.getType());
        assertEquals(sportType, request.getSportType());
        assertEquals(startDateLocal, request.getStartDateLocal());
        assertEquals(elapsedTime, request.getElapsedTime());
        assertEquals(description, request.getDescription());
        assertEquals(distance, request.getDistance());
        assertEquals(trainer, request.getTrainer());
        assertEquals(commute, request.getCommute());
        assertEquals(0.0, request.getCalories());
    }

    @Test
    public void testNoArgsConstructorAndSetters() {
        AddActivityRequest request = new AddActivityRequest();
        request.setName("Evening Ride");
        request.setType("Ride");
        request.setSportType("Cycling");
        request.setStartDateLocal("2025-04-01T18:00:00");
        request.setElapsedTime(5400);
        request.setDescription("Giro in bicicletta serale");
        request.setDistance(25.0f);
        request.setTrainer(0);
        request.setCommute(1);
        request.setCalories(500.0);
        assertEquals("Evening Ride", request.getName());
        assertEquals("Ride", request.getType());
        assertEquals("Cycling", request.getSportType());
        assertEquals("2025-04-01T18:00:00", request.getStartDateLocal());
        assertEquals(5400, request.getElapsedTime());
        assertEquals("Giro in bicicletta serale", request.getDescription());
        assertEquals(25.0f, request.getDistance());
        assertEquals(0, request.getTrainer());
        assertEquals(1, request.getCommute());
        assertEquals(500.0, request.getCalories());
    }

    @Test
    public void testEqualsAndHashCode() {
        AddActivityRequest request1 = new AddActivityRequest("Morning Run", "Run", "Cardio", "2025-04-01T07:30:00", 3600, "Corsa mattutina", 10.5f, 1, 0);
        AddActivityRequest request2 = new AddActivityRequest("Morning Run", "Run", "Cardio", "2025-04-01T07:30:00", 3600, "Corsa mattutina", 10.5f, 1, 0);
        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        request2.setElapsedTime(4000);
        assertNotEquals(request1, request2);
    }
}
