package ch.supsi.sporthub.backend.service.api;

import ch.supsi.sporthub.backend.service.domain.Image;
import ch.supsi.sporthub.backend.service.exception.ServiceException;

/**
 * Service interface for managing image operations within the application.
 * Provides methods for loading, retrieving, setting, and checking the existence of an image.
 */
public interface IImageService {

    /**
     * Loads an image from the specified file path.
     *
     * @param path the path to the image file
     * @throws ServiceException if the image cannot be loaded
     */
    void load(String path) throws ServiceException;


    /**
     * Retrieves the currently loaded or set image.
     *
     * @return the current {@link Image}
     */
    Image get();

    /**
     * Sets the current image in the service context.
     *
     * @param image the {@link Image} to set
     */
    void set(Image image);

    /**
     * Checks if an image has been loaded or set in the service.
     *
     * @return true if an image exists, false otherwise
     */
    boolean imageExists();
}