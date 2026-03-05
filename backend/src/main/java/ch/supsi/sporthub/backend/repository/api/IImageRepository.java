package ch.supsi.sporthub.backend.repository.api;

import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.service.domain.Image;

/**
 * Interface for image repository operations.
 * <p>
 * Defines the contract for loading image resources from a given path.
 */
public interface IImageRepository {

    /**
     * Loads an image from the specified path.
     *
     * @param path the file system or resource path of the image
     * @return the {@link Image} object representing the loaded image
     * @throws RepositoryException if the image cannot be loaded due to access issues or invalid path
     */
    Image load(String path) throws RepositoryException;
}