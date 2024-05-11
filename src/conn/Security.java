package conn;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Security {
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
    }

    public static String encrypt(String message, PublicKey publicKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Split the message into pieces of at most 256 bytes each
        int chunkSize = 128;
        StringBuilder encryptedMessage = new StringBuilder();
        for (int i = 0; i < message.length(); i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, message.length());
            String chunk = message.substring(i, endIndex);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(chunk.getBytes());
            encryptedMessage.append(new String(encrypted));
        }
        return encryptedMessage.toString();
    }

    public static String decrypt(String cipherText, PrivateKey privateKey) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // Split the ciphertext into pieces of at most 256 bytes each
        int chunkSize = 256;
        StringBuilder decryptedMessage = new StringBuilder();
        byte[] cipherBytes = cipherText.getBytes();
        for (int i = 0; i < cipherBytes.length; i += chunkSize) {
            int endIndex = Math.min(i + chunkSize, cipherBytes.length);
            byte[] chunk = new byte[endIndex - i];
            System.arraycopy(cipherBytes, i, chunk, 0, endIndex - i);
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(chunk);
            decryptedMessage.append(new String(decrypted));
        }
        return decryptedMessage.toString();
    }

    public static PublicKey getPublicKeyFromBytes(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }
}
