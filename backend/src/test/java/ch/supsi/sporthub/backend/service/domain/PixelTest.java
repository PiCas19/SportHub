package ch.supsi.sporthub.backend.service.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PixelTest {

    @Test
    void testConstructorAndClamping() {
        Pixel pixel = new Pixel(100, 150, 200);
        assertEquals(100, pixel.getRed());
        assertEquals(150, pixel.getGreen());
        assertEquals(200, pixel.getBlue());
        Pixel pixelBelowZero = new Pixel(-10, -20, -30);
        assertEquals(0, pixelBelowZero.getRed());
        assertEquals(0, pixelBelowZero.getGreen());
        assertEquals(0, pixelBelowZero.getBlue());
        Pixel pixelAbove255 = new Pixel(300, 400, 500);
        assertEquals(255, pixelAbove255.getRed());
        assertEquals(255, pixelAbove255.getGreen());
        assertEquals(255, pixelAbove255.getBlue());
    }

    @Test
    void testStaticFactoryMethods() {
        Pixel blackPixel = Pixel.blackPixel();
        assertEquals(0, blackPixel.getRed());
        assertEquals(0, blackPixel.getGreen());
        assertEquals(0, blackPixel.getBlue());
        Pixel whitePixel = Pixel.whitePixel();
        assertEquals(255, whitePixel.getRed());
        assertEquals(255, whitePixel.getGreen());
        assertEquals(255, whitePixel.getBlue());
    }

    @Test
    void testGettersAndSetters() {
        Pixel pixel = new Pixel(0, 0, 0);
        pixel.setRed(50);
        pixel.setGreen(100);
        pixel.setBlue(150);
        assertEquals(50, pixel.getRed());
        assertEquals(100, pixel.getGreen());
        assertEquals(150, pixel.getBlue());
        pixel.setRed(-10);
        pixel.setGreen(300);
        pixel.setBlue(500);
        assertEquals(-10, pixel.getRed());
        assertEquals(300, pixel.getGreen());
        assertEquals(500, pixel.getBlue());
    }

    @Test
    void testEqualsAndHashCode() {
        Pixel pixel1 = new Pixel(100, 150, 200);
        Pixel pixel2 = new Pixel(100, 150, 200);
        Pixel pixel3 = new Pixel(255, 255, 255);
        assertEquals(pixel1, pixel2);
        assertNotEquals(pixel1, pixel3);
        assertEquals(pixel1.hashCode(), pixel2.hashCode());
        assertNotEquals(pixel1.hashCode(), pixel3.hashCode());
    }

    @Test
    void testToString() {
        Pixel pixel = new Pixel(255, 0, 0);
        assertEquals("16711680", pixel.toString());
        Pixel blackPixel = Pixel.blackPixel();
        assertEquals("0", blackPixel.toString());
        Pixel whitePixel = Pixel.whitePixel();
        assertEquals("16777215", whitePixel.toString());
    }

    @Test
    void testEdgeCases() {
        Pixel minPixel = new Pixel(0, 0, 0);
        assertEquals(0, minPixel.getRed());
        assertEquals(0, minPixel.getGreen());
        assertEquals(0, minPixel.getBlue());
        Pixel maxPixel = new Pixel(255, 255, 255);
        assertEquals(255, maxPixel.getRed());
        assertEquals(255, maxPixel.getGreen());
        assertEquals(255, maxPixel.getBlue());
        Pixel boundaryPixel = new Pixel(-1, 256, 255);
        assertEquals(0, boundaryPixel.getRed());
        assertEquals(255, boundaryPixel.getGreen());
        assertEquals(255, boundaryPixel.getBlue());
    }
}