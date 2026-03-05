package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.repository.api.IImageRepository;
import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.service.domain.Image;
import ch.supsi.sporthub.backend.service.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

    @Mock
    private IImageRepository imageRepository;

    private ImageServiceImpl imageService;

    @BeforeEach
    void setUp() throws Exception {
        imageService = new ImageServiceImpl();
        Field repositoryField = ImageServiceImpl.class.getDeclaredField("imageRepository");
        repositoryField.setAccessible(true);
        repositoryField.set(imageService, imageRepository);
    }

    @Test
    void testLoad_Success() throws Exception {
        String imagePath = "/path/to/image.jpg";
        Image expectedImage = new Image(100, 100);
        when(imageRepository.load(imagePath)).thenReturn(expectedImage);
        imageService.load(imagePath);
        Image result = imageService.get();
        assertNotNull(result);
        assertEquals(expectedImage, result);
        verify(imageRepository).load(imagePath);
    }

    @Test
    void testLoad_ThrowsServiceException() throws Exception {
        String imagePath = "/path/to/nonexistent.jpg";
        String errorMessage = "File not found";
        when(imageRepository.load(imagePath)).thenThrow(new RepositoryException(errorMessage));
        ServiceException exception = assertThrows(ServiceException.class, () -> {
            imageService.load(imagePath);
        });
        assertTrue(exception.getMessage().contains(errorMessage));
        verify(imageRepository).load(imagePath);
    }

    @Test
    void testGet_ReturnsCurrentImage() {
        Image expectedImage = new Image(100, 100);
        imageService.set(expectedImage);
        Image result = imageService.get();
        assertEquals(expectedImage, result);
    }

    @Test
    void testSet_UpdatesCurrentImage() {
        Image newImage = new Image(100, 100);
        imageService.set(newImage);
        assertEquals(newImage, imageService.get());
    }

    @Test
    void testImageExists_ReturnsFalseWhenNull() {
        boolean result = imageService.imageExists();
        assertFalse(result);
    }

    @Test
    void testImageExists_ReturnsTrueWhenNotNull() {
        imageService.set(new Image(100, 100));
        boolean result = imageService.imageExists();
        assertTrue(result);
    }
}