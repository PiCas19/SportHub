package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.repository.api.IImageRepository;
import ch.supsi.sporthub.backend.repository.exception.RepositoryException;
import ch.supsi.sporthub.backend.repository.impl.FileSystemImageRepository;
import ch.supsi.sporthub.backend.service.api.IImageService;
import ch.supsi.sporthub.backend.service.domain.Image;
import ch.supsi.sporthub.backend.service.exception.ServiceException;

/**
 * Implementation of the IImageService interface that manages image loading, retrieval, and storage operations.
 * This service interacts with the underlying image repository to handle image operations, such as loading
 * and setting images, as well as checking if an image exists.
 */
public class ImageServiceImpl implements IImageService {

    private final IImageRepository imageRepository;

    private Image currentImage;

    /**
     * Constructor for the ImageServiceImpl class.
     * Initializes the service with a FileSystemImageRepository for image storage.
     */
    public ImageServiceImpl() {
        this.imageRepository = new FileSystemImageRepository();
    }


    /**
     * Loads an image from the specified path and sets it as the current image.
     *
     * @param path The path of the image to be loaded.
     * @throws ServiceException If an error occurs while loading the image, such as issues with the repository.
     */
    @Override
    public void load(String path) throws ServiceException {
        try {
            currentImage = imageRepository.load(path);
        } catch (RepositoryException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /**
     * Retrieves the current image.
     *
     * @return The current image.
     */
    @Override
    public Image get() {
        return currentImage;
    }

    /**
     * Sets the current image.
     *
     * @param image The image to set as the current image.
     */
    @Override
    public void set(Image image) {
        this.currentImage = image;
    }

    /**
     * Checks if an image is currently set.
     *
     * @return true if the current image exists, false otherwise.
     */
    @Override
    public boolean imageExists() {
        return currentImage != null;
    }


}