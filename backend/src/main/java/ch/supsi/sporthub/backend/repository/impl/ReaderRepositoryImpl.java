package ch.supsi.sporthub.backend.repository.impl;

import ch.supsi.sporthub.backend.repository.api.IReaderRepository;
import ch.supsi.sporthub.backend.repository.impl.reader.ImageReader;
import ch.supsi.sporthub.backend.utils.ReaderLoader;

import java.util.List;

/**
 * Implementation of {@link IReaderRepository} that delegates the loading of image readers
 * to a utility class {@link ReaderLoader}.
 */
public class ReaderRepositoryImpl implements IReaderRepository {

    /**
     * Loads the list of available {@link ImageReader} implementations.
     * @return a list of {@link ImageReader} instances capable of reading supported image formats
     */
    @Override
    public List<ImageReader> loadReaders() {
        return ReaderLoader.loadReaders();
    }
}