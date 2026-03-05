package ch.supsi.sporthub.backend.service.api;


/**
 * Service interface for data encryption and decryption.
 * Provides methods to securely transform plain text to encrypted form and vice versa.
 */
public interface IEncryptionService {

    /**
     * Encrypts the given plain text data.
     *
     * @param data the plain text to encrypt
     * @return the encrypted representation of the input data
     */
    String encrypt(String data);

    /**
     * Decrypts the given encrypted data back to plain text.
     *
     * @param encryptedData the encrypted string to decrypt
     * @return the original plain text
     */
    String decrypt(String encryptedData);
}