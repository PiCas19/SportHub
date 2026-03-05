package ch.supsi.sporthub.backend.repository.impl.reader;

import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.service.domain.Image;
import ch.supsi.sporthub.backend.service.domain.ImageConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


/**
 * Implementation of {@link ImageReader} for reading PNG image files.
 * This reader accepts only PNG files with a maximum size of 2MB.
 */
public class PNGReader implements ImageReader {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB


    /**
     * Validates whether the given file is a supported PNG image and does not exceed the size limit.
     *
     * @param file the file to validate
     * @return true if the file is a PNG and within the allowed size, false otherwise
     */
    @Override
    public boolean validate(File file) {
        return file.getName().toLowerCase().endsWith(".png") && file.length() <= MAX_FILE_SIZE;
    }

    /**
     * Returns the supported file extension for this reader.
     *
     * @return the string "png"
     */
    @Override
    public String getExtension() {
        return "png";
    }

    /**
     * Reads and converts a PNG file into an {@link Image} object.
     *
     * @param file the image file to read
     * @return the converted {@link Image} object
     * @throws RepositoryException if the file is too large, not a valid PNG, or cannot be read
     */
    @Override
    public Image read(File file) throws RepositoryException {
        if (file.length() > MAX_FILE_SIZE) {
            throw new RepositoryException("File size exceeds 2MB: " + file.getName());
        }
        try {
            BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) {
                throw new RepositoryException("Invalid PNG format: " + file.getName());
            }
            return ImageConverter.convertToImage(bufferedImage);
        } catch (IOException e) {
            throw new RepositoryException("Error reading PNG file: " + file.getName(), e);
        }
    }

}