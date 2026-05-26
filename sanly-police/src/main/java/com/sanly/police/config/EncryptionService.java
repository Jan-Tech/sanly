package com.sanly.police.config;

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

    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12, TAG_BITS = 128;
    private final SecretKey key;
    private final SecureRandom rng = new SecureRandom();

    public EncryptionService(@Value("${encryption.key}") String rawKey) {
        this.key = new SecretKeySpec(Arrays.copyOf(rawKey.getBytes(StandardCharsets.UTF_8), 32), "AES");
    }

    public String encrypt(String plain) {
        if (plain == null) return null;
        try {
            byte[] iv = new byte[IV_LEN];
            rng.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[IV_LEN + ct.length];
            System.arraycopy(iv, 0, combined, 0, IV_LEN);
            System.arraycopy(ct, 0, combined, IV_LEN, ct.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) { throw new IllegalStateException("Encryption failed", e); }
    }

    public String decrypt(String encoded) {
        if (encoded == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LEN);
            byte[] ct = Arrays.copyOfRange(combined, IV_LEN, combined.length);
            Cipher cipher = Cipher.getInstance(ALGO);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_BITS, iv));
            return new String(cipher.doFinal(ct), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("Decryption failed", e); }
    }
}
