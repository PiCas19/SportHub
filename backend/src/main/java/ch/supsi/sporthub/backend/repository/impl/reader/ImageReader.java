package ch.supsi.sporthub.backend.repository.impl.reader;

import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.service.domain.Image;
import java.io.File;

/**
 * Interface defining a strategy for reading image files of a specific format.
 * Implementations are responsible for validating and reading files with a supported extension.
 */
public interface ImageReader {

    /**
     * Reads the given image file and returns its data as an {@link Image} object.
     *
     * @param file the image file to be read
     * @return an {@link Image} containing the parsed image data
     * @throws RepositoryException if an error occurs during reading
     */
    Image read(File file) throws RepositoryException;


    /**
     * Validates whether the given file is supported by this reader implementation.
     *
     * @param file the file to validate
     * @return true if the file is supported; false otherwise
     */
    boolean validate(File file);

    /**
     * Returns the file extension supported by this reader (e.g., "jpg", "png").
     *
     * @return the supported file extension
     */
    String getExtension();

}