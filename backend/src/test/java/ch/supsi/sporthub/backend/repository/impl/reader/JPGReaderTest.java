package ch.supsi.sporthub.backend.repository.impl.reader;

import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.service.domain.Image;
import ch.supsi.sporthub.backend.service.domain.ImageConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JPGReaderTest {

    private JPGReader jpgReader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        jpgReader = new JPGReader();
    }

    @Test
    void testValidateValidJPG() throws IOException {
        File file = new File(tempDir.toFile(), "test.jpg");
        file.createNewFile();
        assertThat(jpgReader.validate(file)).isTrue();
    }

    @Test
    void testValidateValidJPEG() throws IOException {
        File file = new File(tempDir.toFile(), "test.jpeg");
        file.createNewFile();
        assertThat(jpgReader.validate(file)).isTrue();
    }

    @Test
    void testValidateInvalidJPG() throws IOException {
        File file = new File(tempDir.toFile(), "test.txt");
        file.createNewFile();
        assertThat(jpgReader.validate(file)).isFalse();
    }

    @Test
    void testGetExtension() {
        assertThat(jpgReader.getExtension()).isEqualTo("jpg");
    }

    @Test
    void testReadValidJPG() throws RepositoryException, IOException {
        File file = new File(tempDir.toFile(), "test.jpg");
        file.createNewFile();

        BufferedImage bufferedImage = mock(BufferedImage.class);
        Image image = mock(Image.class);

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class);
             MockedStatic<ImageConverter> mockedImageConverter = mockStatic(ImageConverter.class)) {

            mockedImageIO.when(() -> ImageIO.read(file)).thenReturn(bufferedImage);
            mockedImageConverter.when(() -> ImageConverter.convertToImage(bufferedImage)).thenReturn(image);

            Image result = jpgReader.read(file);
            assertThat(result).isNotNull();
        }
    }

    @Test
    void testReadInvalidJPGThrowsException() throws IOException {
        File file = new File(tempDir.toFile(), "invalid.jpg");
        file.createNewFile();

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(file)).thenReturn(null);

            assertThatThrownBy(() -> jpgReader.read(file))
                    .isInstanceOf(RepositoryException.class)
                    .hasMessageContaining("Invalid JPG format");
        }
    }

    @Test
    void testReadJPGThrowsIOException() throws IOException {
        File file = new File(tempDir.toFile(), "error.jpg");
        file.createNewFile();

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(file)).thenThrow(new IOException("Simulated IO Exception"));

            assertThatThrownBy(() -> jpgReader.read(file))
                    .isInstanceOf(RepositoryException.class)
                    .hasMessageContaining("Error reading JPG file");
        }
    }

    @Test
    void testReadLargeJPGThrowsException() throws IOException {
        File file = new File(tempDir.toFile(), "large.jpg");
        file.createNewFile();

        File spyFile = spy(file);
        when(spyFile.length()).thenReturn(3 * 1024 * 1024L);

        assertThatThrownBy(() -> jpgReader.read(spyFile))
                .isInstanceOf(RepositoryException.class)
                .hasMessageContaining("File size exceeds 2MB");
    }
    @Test
    void testValidateJPGTooLarge() throws IOException {
        File file = new File(tempDir.toFile(), "test.jpg");
        file.createNewFile();
        File spyFile = spy(file);
        when(spyFile.length()).thenReturn(3 * 1024 * 1024L);

        assertThat(jpgReader.validate(spyFile)).isFalse();
    }

    @Test
    void testValidateInvalidExtensionAndValidSize() throws IOException {
        File file = new File(tempDir.toFile(), "test.txt");
        file.createNewFile();
        File spyFile = spy(file);
        when(spyFile.length()).thenReturn(1024L);

        assertThat(jpgReader.validate(spyFile)).isFalse();
    }

}
