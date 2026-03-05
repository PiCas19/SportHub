package ch.supsi.sporthub.backend.repository.impl;

import ch.supsi.sporthub.backend.repository.api.IImageRepository;
import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.repository.impl.reader.ChainHandler;
import ch.supsi.sporthub.backend.service.domain.Image;


/**
 * Implementation of {@link IImageRepository} that loads images from the file system.
 * Utilizes a {@link ChainHandler} to determine the appropriate reader based on file type and size.
 */
public class FileSystemImageRepository  implements IImageRepository {
    private final ChainHandler chainHandler;


    /**
     * Constructs a FileSystemImageRepository with a default {@link ChainHandler}.
     */
    public FileSystemImageRepository() {
        this.chainHandler = new ChainHandler();
    }

    /**
     * Loads an image from the specified file path using the appropriate image reader.
     *
     * @param path the path to the image file
     * @return an {@link Image} object containing the image data
     * @throws RepositoryException if no compatible reader is found or if an error occurs during reading
     */
    @Override
    public Image load(String path) throws RepositoryException {
        return chainHandler.read(path);
    }

}