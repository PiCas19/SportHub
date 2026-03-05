package ch.supsi.sporthub.backend.service.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ServiceExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String errorMessage = "Test error message";
        ServiceException exception = new ServiceException(errorMessage);
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void testConstructorWithMessageAndCause() {
        String errorMessage = "Test error message";
        Throwable cause = new IllegalArgumentException("Original cause");
        ServiceException exception = new ServiceException(errorMessage, cause);
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertEquals("Original cause", exception.getCause().getMessage());
    }

    @Test
    void testExceptionHierarchy() {
        ServiceException exception = new ServiceException("Test");
        assertInstanceOf(RuntimeException.class, exception);
        assertInstanceOf(Exception.class, exception);
        assertInstanceOf(Throwable.class, exception);
    }

    @Test
    void testStackTracePreservation() {
        Exception originalException = new Exception("Original");
        try {
            throw originalException;
        } catch (Exception e) {
            ServiceException serviceException = new ServiceException("Wrapped", e);
            StackTraceElement[] originalStackTrace = originalException.getStackTrace();
            StackTraceElement[] serviceStackTrace = serviceException.getCause().getStackTrace();
            assertArrayEquals(originalStackTrace, serviceStackTrace);
        }
    }
}