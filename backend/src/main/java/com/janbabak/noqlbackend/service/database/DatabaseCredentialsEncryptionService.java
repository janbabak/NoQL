package com.janbabak.noqlbackend.service.database;

import com.janbabak.noqlbackend.service.utils.EncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Service responsible for encryption and decryption of database credentials.
 * The encryption key comes from environment variable {@code DATA_ENCRYPTION_KEY}.
 */
@Slf4j
@Service
public class DatabaseCredentialsEncryptionService {

    @Value("${app.security.dataEncryptionKey}")
    private String encryptionKey;

    /**
     * Encrypts the credentials using the data encryption key.
     *
     * @param credentials credentials to encrypt
     * @return encrypted credentials
     */
    public String encryptCredentials(String credentials) {
        try {
            return EncryptionUtil.encrypt(credentials, encryptionKey);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            log.error("Error while encrypting credentials: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Decrypts the encrypted credentials using the data encryption key.
     *
     * @param encryptedCredentials credentials to decrypt
     * @return decrypted credentials
     */
    public String decryptCredentials(String encryptedCredentials) {
        try {
            return EncryptionUtil.decrypt(encryptedCredentials, encryptionKey);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            log.error("Error while decrypting credentials: {}", e.getMessage());
        }
        return null;
    }
}
