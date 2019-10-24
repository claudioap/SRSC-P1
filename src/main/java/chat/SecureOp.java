package chat;

import chat.networking.TamperedException;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

public class SecureOp {

    /**
     * Encrypts a plaintext message
     *
     * @param plainText Message
     * @return Ciphertext
     */
    public static byte[] encrypt(Cipher cipher, byte[] plainText, Key sessionKey, IvParameterSpec ivSpec)
            throws InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException,
            IllegalBlockSizeException {
        if (ivSpec == null) {
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, sessionKey, ivSpec);
        }
        cipher.update(plainText);
        return cipher.doFinal();
    }

    /**
     * Decrypts a ciphertext message
     *
     * @param cipherText Message
     * @return Plaintext
     */
    public static byte[] decrypt(Cipher cipher, byte[] cipherText, Key sessionKey, IvParameterSpec ivSpec) {
        try {
            if (ivSpec == null) {
                cipher.init(Cipher.DECRYPT_MODE, sessionKey);
            } else {
                cipher.init(Cipher.DECRYPT_MODE, sessionKey, ivSpec);
            }
            cipher.update(cipherText);
            return cipher.doFinal();
        } catch (InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * Calculates the HMAC for a given message
     *
     * @param data Message
     * @return HMAC
     */
    public static byte[] calculateHMAC(Mac mac, Key key, byte[] data) throws InvalidKeyException {
        mac.init(key);
        mac.update(data);
        return mac.doFinal();
    }

    public static void assertValidHMAC(Mac mac, byte[] data, Key key, byte[] expected) throws InvalidKeyException, TamperedException {
        byte[] hmac = calculateHMAC(mac, key, data);
        if (!Arrays.equals(expected, hmac)) {
            throw new TamperedException(hmac, expected);
        }
    }

    /**
     * Calculates the hash for a given message
     *
     * @param data Message
     * @return Hash
     */
    public static byte[] calculateHash(MessageDigest digest, byte[] data) {
        digest.reset();
        digest.update(data);
        return digest.digest();
    }

    /**
     * Checks if an hash matches a message digest
     *
     * @param data Message
     * @param hash Hash to check against
     * @return Truth value
     */
    public static boolean isValidHash(MessageDigest digest, byte[] data, byte[] hash) {
        return MessageDigest.isEqual(calculateHash(digest, data), hash);
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
