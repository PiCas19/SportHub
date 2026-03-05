package ch.supsi.sporthub.backend.service.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Represents a pixel in an image, with red, green, and blue (RGB) color components.
 * Each color component is an integer value between 0 and 255. The class also provides
 * utility methods for creating black and white pixels, as well as methods for equality and string representation.
 */
public class Pixel {
    @Getter @Setter
    private int red;
    @Getter @Setter
    private int green;
    @Getter @Setter
    private int blue;

    /**
     * Constructs a {@link Pixel} with the specified red, green, and blue components.
     * Each color value is clamped to ensure it is within the valid range of 0 to 255.
     *
     * @param red The red color component (0-255).
     * @param green The green color component (0-255).
     * @param blue The blue color component (0-255).
     */
    public Pixel(int red, int green, int blue) {
        this.red = clamp(red);
        this.green = clamp(green);
        this.blue = clamp(blue);
    }

    /**
     * Clamps a color value to ensure it is between 0 and 255 (inclusive).
     *
     * @param value The color value to clamp.
     * @return The clamped color value.
     */
    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    /**
     * Returns a black pixel (RGB: 0, 0, 0).
     *
     * @return A black {@link Pixel}.
     */
    public static Pixel blackPixel() {
        return new Pixel(0, 0, 0);
    }

    /**
     * Returns a white pixel (RGB: 255, 255, 255).
     *
     * @return A white {@link Pixel}.
     */
    public static Pixel whitePixel() {
        return new Pixel(255, 255, 255);
    }

    /**
     * Compares this pixel to another object for equality.
     * Two pixels are considered equal if their red, green, and blue components are the same.
     *
     * @param o The object to compare this pixel to.
     * @return true if the pixels are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pixel pixel)) return false;
        return red == pixel.red && green == pixel.green && blue == pixel.blue;
    }

    /**
     * Returns a string representation of the pixel in hexadecimal RGB format.
     * The format is a 24-bit integer where the red, green, and blue components are packed into a single integer.
     *
     * @return The string representation of the pixel.
     */
    @Override
    public String toString() {
        return String.valueOf((red << 16) | (green << 8) | blue);
    }


    /**
     * Returns the hash code of this pixel based on its red, green, and blue components.
     * This hash code is useful for storing the pixel in hash-based collections.
     *
     * @return The hash code of this pixel.
     */
    @Override
    public int hashCode() {
        return Objects.hash(red, green, blue);
    }
    
}