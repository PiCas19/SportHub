package ch.supsi.sporthub.backend.repository.impl;

import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.repository.impl.reader.ChainHandler;
import ch.supsi.sporthub.backend.service.domain.Image;
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

public class FileSystemImageRepositoryTest {
    private FileSystemImageRepository fileSystemImageRepository;
    private ChainHandler chainHandler;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        chainHandler = mock(ChainHandler.class);
        fileSystemImageRepository = new FileSystemImageRepository();
    }

    @Test
    void testLoadValidImage() throws RepositoryException, IOException {
        File file = new File(tempDir.toFile(), "valid_image.jpg");
        file.createNewFile();

        BufferedImage bufferedImage = mock(BufferedImage.class);
        when(bufferedImage.getWidth()).thenReturn(100);
        when(bufferedImage.getHeight()).thenReturn(100);
        Image image = mock(Image.class);

        try (MockedStatic<ImageIO> mockedImageIO = mockStatic(ImageIO.class)) {
            mockedImageIO.when(() -> ImageIO.read(file)).thenReturn(bufferedImage);

            when(chainHandler.read(file.getAbsolutePath())).thenReturn(image);

            Image result = fileSystemImageRepository.load(file.getAbsolutePath());
            assertThat(result).isNotNull();
        }
    }

    @Test
    void testLoadInvalidImageThrowsException() throws RepositoryException, IOException {
        File file = new File(tempDir.toFile(), "invalid_image.txt");
        file.createNewFile();

        when(chainHandler.read(file.getAbsolutePath())).thenThrow(new RepositoryException("No compatible readers found for file"));

        assertThatThrownBy(() -> fileSystemImageRepository.load(file.getAbsolutePath()))
                .isInstanceOf(RepositoryException.class)
                .hasMessageContaining("No compatible readers found for file");
    }
}