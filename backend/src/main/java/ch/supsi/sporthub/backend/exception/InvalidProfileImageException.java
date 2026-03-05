package ch.supsi.sporthub.backend.exception;

/**
 * Exception thrown when a provided profile image is invalid.
 * This can occur if the image format is unsupported, the file is corrupted,
 * or the image does not meet required criteria such as size or dimensions.
 */
public class InvalidProfileImageException extends RuntimeException {

    /**
     * Constructs a new InvalidProfileImageException with the specified detail message.
     *
     * @param message the detail message describing the reason for the exception
     */
    public InvalidProfileImageException(String message) {
        super(message);
    }
}