package ch.supsi.sporthub.backend.service.domain;

import java.awt.image.BufferedImage;

/**
 * A utility class for converting a {@link BufferedImage} to an {@link Image} object.
 * This class provides a method to convert a BufferedImage, typically used in Java's standard image processing libraries,
 * into a custom Image object that represents the image with pixels in a 2D array.
 */
public class ImageConverter {

    /**
     * Converts a {@link BufferedImage} to a custom {@link Image}.
     * This method takes a BufferedImage object, extracts its pixel data, and stores it in a new Image object.
     *
     * @param bufferedImage The BufferedImage to convert.
     * @return The resulting custom Image object that represents the same image as the BufferedImage.
     * @throws IllegalArgumentException if the provided BufferedImage is null or has invalid dimensions (non-positive width or height).
     */
    public static Image convertToImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("BufferedImage cannot be null");
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("BufferedImage dimensions must be positive");
        }
        Image image = new Image(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = bufferedImage.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                image.setPixel(x, y, new Pixel(red, green, blue));
            }
        }
        return image;
    }
}