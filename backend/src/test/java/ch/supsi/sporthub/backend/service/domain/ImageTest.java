package ch.supsi.sporthub.backend.service.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImageTest {
    @Test
    void testConstructor() {
        Image image = new Image(10, 10);
        assertEquals(10, image.getWidth());
        assertEquals(10, image.getHeight());
        assertThrows(IllegalArgumentException.class, () -> new Image(0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Image(-5, 10));
        assertThrows(IllegalArgumentException.class, () -> new Image(10, 0));
        assertThrows(IllegalArgumentException.class, () -> new Image(10, -5));
    }

    @Test
    void testInitializePixels() {
        Image image = new Image(3, 3);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                assertEquals(Pixel.blackPixel(), image.getPixels()[y][x]);
            }
        }
    }

    @Test
    void testSetPixel() {
        Image image = new Image(3, 3);
        Pixel redPixel = new Pixel(255, 0, 0);
        image.setPixel(1, 1, redPixel);
        assertEquals(redPixel, image.getPixels()[1][1]);
        image.setPixel(0, 0, redPixel);
        assertEquals(redPixel, image.getPixels()[0][0]);
        image.setPixel(2, 2, redPixel);
        assertEquals(redPixel, image.getPixels()[2][2]);
        assertThrows(IllegalArgumentException.class, () -> image.setPixel(-1, 1, redPixel));
        assertThrows(IllegalArgumentException.class, () -> image.setPixel(3, 1, redPixel));
        assertThrows(IllegalArgumentException.class, () -> image.setPixel(1, -1, redPixel));
        assertThrows(IllegalArgumentException.class, () -> image.setPixel(1, 3, redPixel));
    }

    @Test
    void testEqualsAndHashCode() {
        Image image1 = new Image(2, 2);
        Image image2 = new Image(2, 2);
        Image image3 = new Image(3, 3);
        assertEquals(image1, image2);
        assertNotEquals(image1, image3);
        image1.setPixel(0, 0, new Pixel(255, 0, 0));
        assertNotEquals(image1, image2);
        assertEquals(image1.hashCode(), image1.hashCode());
        assertNotEquals(image1.hashCode(), image2.hashCode());
    }

    @Test
    void testGettersAndSetters() {
        Image image = new Image(10, 10);
        image.setDefaultPath("/path/to/image");
        assertEquals("/path/to/image", image.getDefaultPath());
        image.setDefaultExtension("png");
        assertEquals("png", image.getDefaultExtension());
    }

    @Test
    void testEdgeCases() {
        Image image = new Image(1, 1);
        assertEquals(1, image.getWidth());
        assertEquals(1, image.getHeight());
        Pixel bluePixel = new Pixel(0, 0, 255);
        image.setPixel(0, 0, bluePixel);
        assertEquals(bluePixel, image.getPixels()[0][0]);
    }
}
