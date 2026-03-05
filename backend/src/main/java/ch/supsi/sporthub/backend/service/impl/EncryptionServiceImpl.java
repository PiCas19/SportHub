package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.StravaProperties;
import ch.supsi.sporthub.backend.service.api.IEncryptionService;
import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.stereotype.Service;

/**
 * Implementation of the IEncryptionService interface using AES256 encryption.
 * This service is responsible for encrypting and decrypting data using a password-based AES256 encryption method.
 */
@Service
public class EncryptionServiceImpl implements IEncryptionService {
    private final AES256TextEncryptor textEncryptor;

    /**
     * Constructor for the EncryptionServiceImpl class.
     * Initializes the AES256TextEncryptor with the encryption password from the provided StravaProperties.
     *
     * @param stravaProperties The Strava properties containing the password for the encryption.
     */
    public EncryptionServiceImpl(StravaProperties stravaProperties) {
        String password = stravaProperties.getJasyptEncryptorPassword();
        this.textEncryptor = new AES256TextEncryptor();
        this.textEncryptor.setPassword(password);
    }

    /**
     * Encrypts the provided data using AES256 encryption.
     *
     * @param data The data to be encrypted.
     * @return The encrypted version of the provided data.
     */
    @Override
    public String encrypt(String data) {
        return textEncryptor.encrypt(data);
    }

    /**
     * Decrypts the provided encrypted data using AES256 decryption.
     *
     * @param encryptedData The data to be decrypted.
     * @return The decrypted version of the encrypted data.
     */
    @Override
    public String decrypt(String encryptedData) {
        return textEncryptor.decrypt(encryptedData);
    }
}