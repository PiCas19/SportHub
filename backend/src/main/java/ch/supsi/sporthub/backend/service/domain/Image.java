package ch.supsi.sporthub.backend.service.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents an image consisting of a grid of pixels.
 * The image is defined by its width and height, and each pixel can be set individually.
 * The default state of the pixels is black, and the class allows modification of individual pixels,
 * as well as getting and setting the default path and extension for the image.
 */
public class Image  {
    @Getter
    private Pixel[][] pixels;
    @Getter
    private int width;

    @Getter
    private int height;


    @Getter
    @Setter
    private String defaultPath;


    @Getter
    @Setter
    private String defaultExtension;

    /**
     * Constructs a new image with the specified width and height.
     * Initializes all pixels to the black color.
     *
     * @param width  The width (number of columns) of the image.
     * @param height The height (number of rows) of the image.
     * @throws IllegalArgumentException if width or height are less than or equal to 0.
     */
    public Image(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        this.width = width;
        this.height = height;
        this.pixels = new Pixel[height][width];
        initializePixels();
    }

    /**
     * Initializes all pixels to black pixels.
     * This method is called during image construction.
     */
    private void initializePixels() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y][x] = Pixel.blackPixel();
            }
        }
    }

    /**
     * Sets the color of the pixel at the specified (x, y) coordinates.
     *
     * @param x     The x-coordinate (column index) of the pixel to modify.
     * @param y     The y-coordinate (row index) of the pixel to modify.
     * @param pixel The pixel to set at the given coordinates.
     * @throws IllegalArgumentException if the coordinates are out of bounds.
     */
    public void setPixel(int x, int y, Pixel pixel) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        pixels[y][x] = pixel;
    }


    /**
     * Compares this image to another object for equality.
     * Two images are considered equal if they have the same width, height, and pixel data.
     *
     * @param o The object to compare this image to.
     * @return true if the images are equal, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Image image)) return false;
        return width == image.width && height == image.height && Objects.deepEquals(pixels, image.pixels);
    }

    /**
     * Returns the hash code for this image based on its width, height, and pixel data.
     * This hash code is useful for storing the image in hash-based collections.
     *
     * @return The hash code of this image.
     */
    @Override
    public int hashCode() {
        return Objects.hash(Arrays.deepHashCode(pixels), width, height);
    }

}