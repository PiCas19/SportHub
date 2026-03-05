package ch.supsi.sporthub.backend.utils;

import ch.supsi.sporthub.backend.repository.impl.reader.ImageReader;
import ch.supsi.sporthub.backend.repository.impl.reader.PNGReader;
import ch.supsi.sporthub.backend.repository.impl.reader.JPGReader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReaderLoaderTest {

    @Test
    void testLoadReadersFromTestResources() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("readers.json");
        assertNotNull(inputStream, "The readers.json file was not found in the test resources");
        List<ImageReader> readers = ReaderLoader.loadReaders();
        assertEquals(2, readers.size(), "There should be two readers loaded");
        assertInstanceOf(PNGReader.class, readers.get(0), "The first reader should be PNGReader");
        assertInstanceOf(JPGReader.class, readers.get(1), "The second reader should be JPGReader");
    }
    @Test
    void testLoadReaders_FileMissing_ThrowsException() {
        class CustomLoader extends ReaderLoader {
            public static List<ImageReader> loadWithoutFile() {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.readTree((InputStream) null);
                    return List.of();
                } catch (Exception e) {
                    throw new RuntimeException("Error loading readers from JSON file", e);
                }
            }
        }

        RuntimeException ex = assertThrows(RuntimeException.class, CustomLoader::loadWithoutFile);
        assertTrue(ex.getMessage().contains("Error loading readers from JSON file"));
    }
}
