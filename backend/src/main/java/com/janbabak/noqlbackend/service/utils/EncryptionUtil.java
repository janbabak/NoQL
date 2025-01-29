package com.janbabak.noqlbackend.service.utils;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for encryption and decryption of strings using AES/GCM/NoPadding.
 */
@Slf4j
public class EncryptionUtil {

    private static final String AES = "AES";
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 16; // 128 bits
    private static final int GCM_IV_LENGTH = 12; // 96 bits

    /**
     * Encrypt a plaintext string.
     *
     * @param plainText plaintext string to encrypt
     * @param base64Key base64 encoded key (length before encoding is 256 bits)
     * @return base64 encoded encrypted data
     * @throws NoSuchPaddingException             if no such padding is available, should not happen
     * @throws NoSuchAlgorithmException           if no such algorithm is available, should not happen
     * @throws InvalidAlgorithmParameterException if the algorithm parameters are invalid, should not happen
     * @throws InvalidKeyException                if the key is invalid
     * @throws IllegalBlockSizeException          if the block size is invalid, should not happen
     * @throws BadPaddingException                should not happen
     */
    public static String encrypt(String plainText, String base64Key) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        SecretKey secretKey = getKeyFromBase64(base64Key);
        byte[] iv = generateIV();

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

        byte[] cipherText = cipher.doFinal(plainText.getBytes());
        byte[] encryptedData = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, iv.length); // copy iv to the beginning of the array
        System.arraycopy(cipherText, 0, encryptedData, iv.length, cipherText.length); // copy (append) cipherText

        return Base64.getEncoder().encodeToString(encryptedData);
    }

    /**
     * Decrypt a base64 encoded ciphertext.
     *
     * @param cipherText base64 encoded ciphertext
     * @param base64Key  base64 encoded key (length before encoding is 256 bits)
     * @return decrypted plaintext
     * @throws NoSuchPaddingException             if no such padding is available, should not happen
     * @throws NoSuchAlgorithmException           if no such algorithm is available, should not happen
     * @throws InvalidAlgorithmParameterException if the algorithm parameters are invalid
     * @throws InvalidKeyException                if the key is invalid
     * @throws IllegalBlockSizeException          if the block size is invalid, should not happen
     * @throws BadPaddingException,               should not happen
     */
    public static String decrypt(String cipherText, String base64Key) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException,
            IllegalBlockSizeException, BadPaddingException {

        SecretKey secretKey = getKeyFromBase64(base64Key);
        byte[] decodedData = Base64.getDecoder().decode(cipherText);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(decodedData, 0, iv, 0, iv.length);

        byte[] encryptedText = new byte[decodedData.length - GCM_IV_LENGTH];
        System.arraycopy(decodedData, GCM_IV_LENGTH, encryptedText, 0, encryptedText.length);

        Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] plainText = cipher.doFinal(encryptedText);
        return new String(plainText);
    }

    /**
     * Convert a base64 string to a SecretKey
     *
     * @param base64Key base64 encoded key
     * @return SecretKey
     */
    private static SecretKey getKeyFromBase64(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, AES);
    }

    /**
     * Generate a random initialization vector
     *
     * @return byte[] iv
     */
    private static byte[] generateIV() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
