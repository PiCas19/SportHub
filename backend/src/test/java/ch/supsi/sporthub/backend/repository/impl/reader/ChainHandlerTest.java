package ch.supsi.sporthub.backend.repository.impl.reader;

import ch.supsi.sporthub.backend.repository.api.IReaderRepository;
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
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class ChainHandlerTest {

    private ChainHandler chainHandler;
    private IReaderRepository readerRepository;
    private ImageReader jpgReader;
    private ImageReader pngReader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        readerRepository = mock(IReaderRepository.class);
        chainHandler = new ChainHandler();
        jpgReader = mock(ImageReader.class);
        pngReader = mock(ImageReader.class);
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

            when(jpgReader.validate(file)).thenReturn(true);
            when(jpgReader.read(file)).thenReturn(image);
            when(readerRepository.loadReaders()).thenReturn(List.of(jpgReader));

            Image result = chainHandler.read(file.getAbsolutePath());
            assertThat(result).isNotNull();
        }
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

            when(pngReader.validate(file)).thenReturn(true);
            when(pngReader.read(file)).thenReturn(image);
            when(readerRepository.loadReaders()).thenReturn(List.of(pngReader));

            Image result = chainHandler.read(file.getAbsolutePath());
            assertThat(result).isNotNull();
        }
    }

    @Test
    void testReadInvalidFileThrowsException() throws IOException {
        File file = new File(tempDir.toFile(), "test.txt");
        file.createNewFile();

        when(readerRepository.loadReaders()).thenReturn(List.of(jpgReader, pngReader));

        assertThatThrownBy(() -> chainHandler.read(file.getAbsolutePath()))
                .isInstanceOf(RepositoryException.class)
                .hasMessageContaining("No compatible readers found for file");
    }
}
