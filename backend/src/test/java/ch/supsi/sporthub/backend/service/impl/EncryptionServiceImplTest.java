package ch.supsi.sporthub.backend.service.impl;

import ch.supsi.sporthub.backend.config.StravaProperties;
import org.jasypt.util.text.AES256TextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EncryptionServiceImplTest {

    @Mock
    private StravaProperties stravaProperties;

    private EncryptionServiceImpl encryptionService;

    private String encryptionPassword = "testPassword";
    private String plainData = "sensitiveData";
    private String encryptedData = "someEncryptedData";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(stravaProperties.getJasyptEncryptorPassword()).thenReturn(encryptionPassword);
        encryptionService = new EncryptionServiceImpl(stravaProperties);
    }

    @Test
    void testConstructor() {
        verify(stravaProperties).getJasyptEncryptorPassword();
    }

    @Test
    void testEncrypt() throws NoSuchFieldException, IllegalAccessException {
        AES256TextEncryptor mockEncryptor = mock(AES256TextEncryptor.class);
        when(mockEncryptor.encrypt(plainData)).thenReturn(encryptedData);
        Field field = EncryptionServiceImpl.class.getDeclaredField("textEncryptor");
        field.setAccessible(true);
        field.set(encryptionService, mockEncryptor);

        String result = encryptionService.encrypt(plainData);
        assertEquals(encryptedData, result);
        verify(mockEncryptor).encrypt(plainData);
    }

    @Test
    void testDecrypt() throws NoSuchFieldException, IllegalAccessException {
        AES256TextEncryptor mockEncryptor = mock(AES256TextEncryptor.class);
        when(mockEncryptor.decrypt(encryptedData)).thenReturn(plainData);
        Field field = EncryptionServiceImpl.class.getDeclaredField("textEncryptor");
        field.setAccessible(true);
        field.set(encryptionService, mockEncryptor);

        String result = encryptionService.decrypt(encryptedData);
        assertEquals(plainData, result);
        verify(mockEncryptor).decrypt(encryptedData);
    }
}