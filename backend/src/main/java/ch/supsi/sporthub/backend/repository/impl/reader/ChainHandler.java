package ch.supsi.sporthub.backend.repository.impl.reader;

import ch.supsi.sporthub.backend.repository.api.IReaderRepository;
import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.repository.impl.ReaderRepositoryImpl;
import ch.supsi.sporthub.backend.service.domain.Image;


import java.io.File;
import java.util.List;

/**
 * Handles the reading of image files by delegating the task to a suitable reader from a chain of registered readers.
 * <p>
 * This class uses the chain of responsibility pattern to identify a compatible {@link ImageReader}
 * for the given file and delegates the reading task to it. If no suitable reader is found,
 * a {@link RepositoryException} is thrown.
 */
public class ChainHandler {
    private final IReaderRepository readerRepository;

    /**
     * Constructs a new {@code ChainHandler} with a default {@link ReaderRepositoryImpl} implementation.
     */
    public ChainHandler() {
        this.readerRepository = new ReaderRepositoryImpl();
    }

    /**
     * Attempts to read an image file using the first compatible {@link ImageReader}.
     *
     * @param filePath the path to the image file to be read
     * @return an {@link Image} object containing the parsed image data
     * @throws RepositoryException if no compatible reader is found for the specified file
     */
    public Image read(String filePath) throws RepositoryException {
        File file = new File(filePath);
        List<ImageReader> readers = readerRepository.loadReaders();;

        for (ImageReader reader : readers) {
            if (reader.validate(file)) {
                return reader.read(file);
            }
        }
        throw new RepositoryException("No compatible readers found for file: " + filePath);
    }

}