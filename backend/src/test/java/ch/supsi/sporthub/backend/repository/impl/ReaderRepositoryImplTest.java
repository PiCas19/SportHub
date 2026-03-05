package ch.supsi.sporthub.backend.repository.impl;

import ch.supsi.sporthub.backend.repository.api.IReaderRepository;
import ch.supsi.sporthub.backend.repository.impl.reader.ImageReader;
import ch.supsi.sporthub.backend.utils.ReaderLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

public class ReaderRepositoryImplTest {

    private IReaderRepository readerRepository;

    @BeforeEach
    void setUp() {
        readerRepository = new ReaderRepositoryImpl();
    }

    @Test
    void testLoadReaders() {
        List<ImageReader> mockReaders = List.of(mock(ImageReader.class));

        try (MockedStatic<ReaderLoader> mockedReaderLoader = mockStatic(ReaderLoader.class)) {
            mockedReaderLoader.when(ReaderLoader::loadReaders).thenReturn(mockReaders);

            List<ImageReader> readers = readerRepository.loadReaders();
            assertThat(readers).isNotEmpty();
            assertThat(readers).isEqualTo(mockReaders);
        }
    }
}
