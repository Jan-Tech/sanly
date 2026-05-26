package com.sanly.banking.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM encryption for sensitive fields (e.g., consent purpose).
 */
@Slf4j
@Service
public class EncryptionService {

    private static final String ALGORITHM     = "AES/GCM/NoPadding";
    private static final int    GCM_IV_LENGTH = 12; // 96 bits
    private static final int    GCM_TAG_BITS  = 128;

    private final SecretKey secretKey;

    public EncryptionService(@Value("${encryption.key}") String keyBase64) {
        byte[] keyBytes = Base64.getDecoder().decode(keyBase64);
        if (keyBytes.length != 32) {
            throw new IllegalStateException("ENCRYPTION_KEY must be a 256-bit (32-byte) Base64-encoded key");
        }
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Returns Base64-encoded (IV + ciphertext).
     */
    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buf = ByteBuffer.allocate(iv.length + ciphertext.length);
            buf.put(iv);
            buf.put(ciphertext);

            return Base64.getEncoder().encodeToString(buf.array());
        } catch (Exception e) {
            log.error("Encryption failed", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64-encoded (IV + ciphertext) string.
     */
    public String decrypt(String ciphertext) {
        try {
            byte[] data = Base64.getDecoder().decode(ciphertext);
            ByteBuffer buf = ByteBuffer.wrap(data);

            byte[] iv = new byte[GCM_IV_LENGTH];
            buf.get(iv);
            byte[] encryptedBytes = new byte[buf.remaining()];
            buf.get(encryptedBytes);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            return new String(cipher.doFinal(encryptedBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
