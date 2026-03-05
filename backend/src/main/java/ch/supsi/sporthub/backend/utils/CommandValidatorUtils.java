package ch.supsi.sporthub.backend.utils;

import java.util.Map;

/**
 * Utility class for command validation, specifically to check if a map of parameters contains the required keys.
 * This is useful for ensuring that necessary parameters are provided before executing a command or performing an action.
 */
public class CommandValidatorUtils {

    /**
     * Checks if the given map of parameters contains all the required keys.
     * The method iterates through the list of required keys and ensures that each key exists in the provided parameters map.
     *
     * @param params        The map of parameters to validate.
     * @param requiredKeys  The required keys that must be present in the map.
     * @return true if all required keys are present, false otherwise.
     */
    public static boolean hasRequiredParams(Map<String, String> params, String... requiredKeys) {
        for (String key : requiredKeys) {
            if (!params.containsKey(key)) return false;
        }
        return true;
    }
}