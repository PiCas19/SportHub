package ch.supsi.sporthub.backend.utils;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CommandValidatorUtilsTest {

    @Test
    void testHasRequiredParams_AllPresent() {
        Map<String, String> params = new HashMap<>();
        params.put("user", "admin");
        params.put("pass", "1234");

        boolean result = CommandValidatorUtils.hasRequiredParams(params, "user", "pass");
        assertTrue(result);
    }

    @Test
    void testHasRequiredParams_MissingOneKey() {
        Map<String, String> params = new HashMap<>();
        params.put("user", "admin");

        boolean result = CommandValidatorUtils.hasRequiredParams(params, "user", "pass");
        assertFalse(result);
    }

    @Test
    void testHasRequiredParams_NoRequiredKeys() {
        Map<String, String> params = new HashMap<>();
        params.put("x", "y");

        boolean result = CommandValidatorUtils.hasRequiredParams(params);
        assertTrue(result);
    }

    @Test
    void testHasRequiredParams_EmptyParamsMap_WithRequiredKeys() {
        Map<String, String> params = new HashMap<>();

        boolean result = CommandValidatorUtils.hasRequiredParams(params, "token");
        assertFalse(result);
    }

    @Test
    void testHasRequiredParams_NullAndEmptyValuesButKeyPresent() {
        Map<String, String> params = new HashMap<>();
        params.put("user", null);
        params.put("pass", "");

        boolean result = CommandValidatorUtils.hasRequiredParams(params, "user", "pass");
        assertTrue(result);
    }
}
