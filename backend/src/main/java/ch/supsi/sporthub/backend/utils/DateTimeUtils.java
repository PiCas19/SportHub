
package ch.supsi.sporthub.backend.utils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for working with date and time operations.
 * Provides methods to format elapsed time and manage date formatting.
 */
public class DateTimeUtils {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    private DateTimeUtils() {
    }

    /**
     * Formats the elapsed time from seconds into a string representation in "HH:mm:ss" format.
     *
     * @param elapsedSeconds The elapsed time in seconds.
     * @return A string representing the elapsed time in "HH:mm:ss" format.
     */
    public static String formatElapsedTime(int elapsedSeconds) {
        Duration duration = Duration.ofSeconds(elapsedSeconds);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}