package utils.crypto;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public final class CryptoUtil {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ENV = "FRAMEWORK_SECRET_KEY";
    private static final int KEY_LENGTH_BYTES = 32;   // 256-bit
    private static final int IV_LENGTH_BYTES = 12;    // recommended for GCM
    private static final int TAG_LENGTH_BITS = 128;   // max-strength auth tag
    private static final SecureRandom RANDOM = new SecureRandom();

    private CryptoUtil() { }

    // ---------- Public API (uses key from .env variable) ----------

    public static String encrypt(String plainText) {
        return encrypt(plainText, loadKey());
    }

    public static String decrypt(String encrypted) {
        return decrypt(encrypted, loadKey());
    }

    // ---------- Overloads with explicit key (used by unit tests) ----------

    public static String encrypt(String plainText, SecretKey key) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("Nothing to encrypt");
        }
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            RANDOM.nextBytes(iv);                           // fresh IV every time

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Output format: Base64( IV | ciphertext+tag )
            byte[] combined = ByteBuffer.allocate(iv.length + cipherText.length)
                    .put(iv).put(cipherText).array();
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public static String decrypt(String encrypted, SecretKey key) {
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(encrypted.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Encrypted value is not valid Base64");
        }
        if (decoded.length <= IV_LENGTH_BYTES + TAG_LENGTH_BITS / 8) {
            throw new IllegalStateException("Encrypted value is too short / corrupted");
        }
        try {
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (AEADBadTagException e) {
            throw new IllegalStateException("Decryption failed: wrong key or value was tampered with");
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }

    // ---------- Key loading ----------

    static SecretKey loadKey() {
        String base64Key = System.getenv(KEY_ENV);
        if (base64Key == null || base64Key.isBlank()) {
            throw new IllegalStateException(KEY_ENV + " environment variable is not set");
        }
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(base64Key.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(KEY_ENV + " is not valid Base64");
        }
        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(KEY_ENV + " must be a 256-bit (32-byte) key, found "
                    + keyBytes.length + " bytes");
        }
        return new SecretKeySpec(keyBytes, "AES");
    }
}