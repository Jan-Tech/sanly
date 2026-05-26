package com.sanly.medical.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-GCM field-level encryption for the {@code notes} column.
 * Each encrypted value is Base64(IV[12] || ciphertext || GCM-tag[16]).
 */
@Component
public class EncryptionService {

    private static final String ALGORITHM      = "AES/GCM/NoPadding";
    private static final int    IV_LEN         = 12;
    private static final int    TAG_BITS       = 128;

    private final SecretKey   secretKey;
    private final SecureRandom rng = new SecureRandom();

    public EncryptionService(@Value("${encryption.key}") String rawKey) {
        byte[] keyBytes = Arrays.copyOf(rawKey.getBytes(StandardCharsets.UTF_8), 32);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        if (plaintext == null) return null;
        try {
            byte[] iv = new byte[IV_LEN];
            rng.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[IV_LEN + ct.length];
            System.arraycopy(iv, 0, combined, 0,       IV_LEN);
            System.arraycopy(ct, 0, combined, IV_LEN,  ct.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String encoded) {
        if (encoded == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LEN);
            byte[] ct = Arrays.copyOfRange(combined, IV_LEN, combined.length);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
