package com.sanly.land.config;

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
        this.key = new SecretKeySpec(Arrays.copyOf(rawKey.getBytes(StandardCharsets.UTF_8), 32), "AES");
    }

    public String encrypt(String plain) {
        if (plain == null) return null;
        try {
            byte[] iv = new byte[12]; rng.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[12 + ct.length];
            System.arraycopy(iv, 0, out, 0, 12); System.arraycopy(ct, 0, out, 12, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (Exception e) { throw new IllegalStateException("Encryption failed", e); }
    }

    public String decrypt(String encoded) {
        if (encoded == null) return null;
        try {
            byte[] combined = Base64.getDecoder().decode(encoded);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, Arrays.copyOfRange(combined, 0, 12)));
            return new String(c.doFinal(Arrays.copyOfRange(combined, 12, combined.length)), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("Decryption failed", e); }
    }
}
