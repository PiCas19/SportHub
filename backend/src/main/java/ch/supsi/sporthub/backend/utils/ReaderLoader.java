package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.repository.impl.reader.ImageReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class that loads ImageReader instances from a JSON configuration file.
 * The class loads the readers' class names from a JSON file and dynamically instantiates them.
 */
public class ReaderLoader {

    /**
     * Utility class that loads ImageReader instances from a JSON configuration file.
     * The class loads the readers' class names from a JSON file and dynamically instantiates them.
     */
    public static List<ImageReader> loadReaders() {
        List<ImageReader> readers = new ArrayList<>();

        try {
            InputStream inputStream = ReaderLoader.class.getClassLoader().getResourceAsStream("readers.json");
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(inputStream);

            for (JsonNode node : rootNode.get("readers")) {
                String className = node.asText();
                try {
                    Class<?> clazz = Class.forName(className);
                    Object instance = clazz.getDeclaredConstructor().newInstance();
                    if (instance instanceof ImageReader) {
                        readers.add((ImageReader) instance);
                    }
                } catch (Exception e) {
                    System.err.println("Could not load reader: " + className + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading readers from JSON file", e);
        }

        return readers;
    }
}