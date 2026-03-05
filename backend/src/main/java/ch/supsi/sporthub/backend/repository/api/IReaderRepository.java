package ch.supsi.sporthub.backend.repository.api;

import ch.supsi.sporthub.backend.repository.impl.reader.ImageReader;

import java.util.List;

/**
 * Interface for accessing image reader implementations.
 * <p>
 * Provides a method to retrieve all available {@link ImageReader} instances
 * used to handle different image formats or sources.
 */
public interface IReaderRepository {

    /**
     * Loads and returns a list of all available image readers.
     *
     * @return a list of {@link ImageReader} instances
     */
    List<ImageReader> loadReaders();
}