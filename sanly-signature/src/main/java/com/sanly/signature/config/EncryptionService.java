package com.sanly.signature.config;

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

@Component
public class EncryptionService {

    private final SecretKey key;
    private final SecureRandom rng = new SecureRandom();

    public EncryptionService(@Value("${encryption.key}") String rawKey) {
        byte[] keyBytes = Arrays.copyOf(rawKey.getBytes(StandardCharsets.UTF_8), 32);
        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypts plaintext using AES-256-GCM.
     * Output format: Base64(12-byte IV || ciphertext+tag)
     */
    public String encrypt(String plain) {
        if (plain == null) return null;
        try {
            byte[] iv = new byte[12];
            rng.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] ciphertext = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[12 + ciphertext.length];
            System.arraycopy(iv, 0, combined, 0, 12);
            System.arraycopy(ciphertext, 0, combined, 12, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    /**
     * Decrypts a Base64-encoded AES-256-GCM ciphertext (IV prefix format).
     */
    public String decrypt(String encoded) {
        if (encoded == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            byte[] iv = Arrays.copyOfRange(combined, 0, 12);
            byte[] ciphertext = Arrays.copyOfRange(combined, 12, combined.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}
