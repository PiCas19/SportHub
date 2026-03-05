package ch.supsi.sporthub.backend.repository.impl.reader;

import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.service.domain.Image;
import ch.supsi.sporthub.backend.service.domain.ImageConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Implementation of {@link ImageReader} for reading JPG and JPEG image files.
 * This reader only accepts files up to 2MB in size and ensures the image is valid before reading.
 */
public class JPGReader implements ImageReader {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    /**
     * Validates whether the given file is a supported JPG/JPEG image and does not exceed the size limit.
     *
     * @param file the file to validate
     * @return true if the file is a valid JPG/JPEG and within size limits, false otherwise
     */
    @Override
    public boolean validate(File file) {
        return (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".jpeg"))
                && file.length() <= MAX_FILE_SIZE;
    }

    /**
     * Returns the file extension supported by this reader.
     *
     * @return the string "jpg"
     */
    @Override
    public String getExtension() {
        return "jpg";
    }

    /**
     * Reads and converts a JPG/JPEG file into an {@link Image} object.
     *
     * @param file the image file to read
     * @return the converted {@link Image} object
     * @throws RepositoryException if the file is too large, not a valid image, or cannot be read
     */
    @Override
    public Image read(File file) throws RepositoryException {
        if (file.length() > MAX_FILE_SIZE) {
            throw new RepositoryException("File size exceeds 2MB: " + file.getName());
        }
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) {
                throw new RepositoryException("Invalid JPG format: " + file.getName());
            }
            return ImageConverter.convertToImage(bufferedImage);
        } catch (IOException e) {
            throw new RepositoryException("Error reading JPG file: " + file.getName(), e);
        }
    }
}