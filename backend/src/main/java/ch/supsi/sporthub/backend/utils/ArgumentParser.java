package ch.supsi.sporthub.backend.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to parse command-line arguments in the format of key-value pairs.
 * This class processes an array of arguments and returns a map where the keys are the argument names,
 * and the values are the associated argument values.
 */
public class ArgumentParser {

    /**
     * Parses the provided array of arguments and returns a map of key-value pairs.
     * Each argument should be in the format of "key=value", and multiple values can be combined by spaces
     * if the value is enclosed in quotes.
     * Example input: ["key1=value1", "key2=value2", "key3=value3 part"]
     * Example output: { "key1" => "value1", "key2" => "value2", "key3" => "value3 part" }
     *
     * @param args Array of strings representing the command-line arguments.
     * @return A map where each key is an argument name, and each value is the associated argument value.
     */
    public static Map<String, String> parseArgs(String[] args) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].contains("=")) continue;

            String[] parts = args[i].split("=", 2);
            String key = parts[0].trim().toLowerCase();
            StringBuilder value = new StringBuilder(parts[1].trim().replaceAll("^\"|\"$", ""));
            while (i + 1 < args.length && !args[i + 1].contains("=")) {
                value.append(" ").append(args[++i].trim().replaceAll("^\"|\"$", ""));
            }
            result.put(key, value.toString());
        }
        return result;
    }
}