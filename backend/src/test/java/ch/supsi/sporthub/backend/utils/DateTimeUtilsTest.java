package ch.supsi.sporthub.backend.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateTimeUtilsTest {

    @Test
    void testFormatElapsedTime_OnlySeconds() {
        assertEquals("00:00:05", DateTimeUtils.formatElapsedTime(5));
    }

    @Test
    void testFormatElapsedTime_MinutesAndSeconds() {
        assertEquals("00:01:30", DateTimeUtils.formatElapsedTime(90));
    }

    @Test
    void testFormatElapsedTime_HoursMinutesSeconds() {
        assertEquals("01:02:03", DateTimeUtils.formatElapsedTime(3723));
    }

    @Test
    void testFormatElapsedTime_Zero() {
        assertEquals("00:00:00", DateTimeUtils.formatElapsedTime(0));
    }
}
