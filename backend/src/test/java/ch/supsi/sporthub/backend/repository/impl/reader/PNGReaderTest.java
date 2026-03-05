package ch.supsi.sporthub.backend.repository.impl.reader;

import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.service.domain.Image;
import ch.supsi.sporthub.backend.service.domain.ImageConverter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class PNGReaderTest {
    private PNGReader pngReader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pngReader = new PNGReader();
    }

    @Test
    void testValidateValidPNG() throws IOException {
        File file = new File(tempDir.toFile(), "test.png");
        file.createNewFile();
        assertThat(pngReader.validate(file)).isTrue();
    }

    @Test
    void testValidateInvalidPNG() throws IOException {
        File file = new File(tempDir.toFile(), "test.txt");
        file.createNewFile();
        assertThat(pngReader.validate(file)).isFalse();
    }

    @Test
    void testGetExtension() {
        assertThat(pngReader.getExtension()).isEqualTo("png");
    }

    @Test
    void testReadValidPNG() throws RepositoryException, IOException {
        File file = new File(tempDir.toFile(), "test.png");
        file.createNewFile();

        BufferedImage bufferedImage = mock(BufferedImage.class);
        Image image = mock(Image.class);

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class);
             MockedStatic<ImageConverter> mockedImageConverter = mockStatic(ImageConverter.class)) {

            mockedImageIO.when(() -> ImageIO.read(file)).thenReturn(bufferedImage);
            mockedImageConverter.when(() -> ImageConverter.convertToImage(bufferedImage)).thenReturn(image);

            Image result = pngReader.read(file);
            Assertions.assertThat(result).isNotNull();
        }
    }

    @Test
    void testReadInvalidPNGThrowsException() throws IOException {
        File file = new File(tempDir.toFile(), "invalid.png");
        file.createNewFile();

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(file)).thenReturn(null);

            Assertions.assertThatThrownBy(() -> pngReader.read(file))
                    .isInstanceOf(RepositoryException.class)
                    .hasMessageContaining("Invalid PNG format");
        }
    }

    @Test
    void testReadPNGThrowsIOException() throws IOException {
        File file = new File(tempDir.toFile(), "error.png");
        file.createNewFile();

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(file)).thenThrow(new IOException("Simulated IO Exception"));

            Assertions.assertThatThrownBy(() -> pngReader.read(file))
                    .isInstanceOf(RepositoryException.class)
                    .hasMessageContaining("Error reading PNG file");
        }
    }

    @Test
    void testReadLargePNGThrowsException() throws IOException {
        File file = new File(tempDir.toFile(), "large.png");
        file.createNewFile();
        File spyFile = spy(file);
        when(spyFile.length()).thenReturn(3 * 1024 * 1024L);

        Assertions.assertThatThrownBy(() -> pngReader.read(spyFile))
                .isInstanceOf(RepositoryException.class)
                .hasMessageContaining("File size exceeds 2MB");
    }

    @Test
    void testValidateTooLargePNG() throws IOException {
        File file = new File(tempDir.toFile(), "large.png");
        file.createNewFile();
        File spyFile = spy(file);
        when(spyFile.length()).thenReturn(3 * 1024 * 1024L);

        assertThat(new PNGReader().validate(spyFile)).isFalse();
    }

    @Test
    void testValidateNonPNGSmallFile() throws IOException {
        File file = new File(tempDir.toFile(), "not_image.txt");
        file.createNewFile();
        File spyFile = spy(file);
        when(spyFile.length()).thenReturn(500L);

        assertThat(new PNGReader().validate(spyFile)).isFalse();
    }

    @Test
    void testValidateNonPNGTooLarge() throws IOException {
        File file = new File(tempDir.toFile(), "not_image.txt");
        file.createNewFile();
        File spyFile = spy(file);
        when(spyFile.length()).thenReturn(3 * 1024 * 1024L);

        assertThat(new PNGReader().validate(spyFile)).isFalse();
    }

}
