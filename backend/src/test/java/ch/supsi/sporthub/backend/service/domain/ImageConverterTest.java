package ch.supsi.sporthub.backend.service.domain;

import org.junit.jupiter.api.Test;
import java.awt.image.BufferedImage;
import static org.junit.jupiter.api.Assertions.*;

public class ImageConverterTest {

    @Test
    void testConvertToImage() {
        BufferedImage bufferedImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        bufferedImage.setRGB(0, 0, 0xFF0000);
        bufferedImage.setRGB(1, 0, 0x00FF00);
        bufferedImage.setRGB(0, 1, 0x0000FF);
        bufferedImage.setRGB(1, 1, 0xFFFFFF);
        Image image = ImageConverter.convertToImage(bufferedImage);
        assertEquals(2, image.getWidth());
        assertEquals(2, image.getHeight());
        assertEquals(new Pixel(255, 0, 0), image.getPixels()[0][0]);
        assertEquals(new Pixel(0, 255, 0), image.getPixels()[0][1]);
        assertEquals(new Pixel(0, 0, 255), image.getPixels()[1][0]);
        assertEquals(new Pixel(255, 255, 255), image.getPixels()[1][1]);
    }

    @Test
    void testConvertToImageWithNullInput() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            ImageConverter.convertToImage(null);
        });
        assertEquals("BufferedImage cannot be null", exception.getMessage());
    }

    @Test
    void testConvertToImageWithSinglePixel() {
        BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        bufferedImage.setRGB(0, 0, 0x123456);
        Image image = ImageConverter.convertToImage(bufferedImage);
        assertEquals(1, image.getWidth());
        assertEquals(1, image.getHeight());
        int red = (0x123456 >> 16) & 0xFF;
        int green = (0x123456 >> 8) & 0xFF;
        int blue = 0x123456 & 0xFF;
        assertEquals(new Pixel(red, green, blue), image.getPixels()[0][0]);
    }
}