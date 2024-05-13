package conn;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;

public class EncryptionTest {

    public static PublicKey stringToPublicKey(String publicKeyStr) throws Exception {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static PrivateKey stringToPrivateKey(String privateKeyStr) throws Exception {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public static byte[] encryptMessage(String message, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(message.getBytes());
    }

    public static byte[] decryptMessage(byte[] encryptedMessage, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedMessage);
    }

    public static String encryptAndConcatenate(String message, PublicKey publicKey) throws Exception {
        // Split the message into chunks of 245 bytes
        List<byte[]> encryptedChunks = new ArrayList<>();
        int offset = 0;
        while (offset < message.length()) {
            int chunkSize = Math.min(245, message.length() - offset);
            String chunk = message.substring(offset, offset + chunkSize);
            byte[] encryptedChunk = encryptMessage(chunk, publicKey);
            encryptedChunks.add(encryptedChunk);
            offset += chunkSize;
        }
        
        // Concatenate encrypted chunks with ampersands
        StringBuilder concatenated = new StringBuilder();
        for (int i = 0; i < encryptedChunks.size(); i++) {
            if (i > 0) {
                concatenated.append("&");
            }
            concatenated.append(Base64.getEncoder().encodeToString(encryptedChunks.get(i)));
        }
        return concatenated.toString();
    }

    public static String decryptAndConcatenate(String concatenatedMessage, PrivateKey privateKey) throws Exception {
        // Split the concatenated message by ampersands
        String[] encryptedChunks = concatenatedMessage.split("&");
        
        // Decrypt each chunk separately and concatenate them
        StringBuilder decrypted = new StringBuilder();
        for (String encryptedChunk : encryptedChunks) {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedChunk);
            byte[] decryptedBytes = decryptMessage(encryptedBytes, privateKey);
            decrypted.append(new String(decryptedBytes));
        }
        return decrypted.toString();
    }

    public static byte[] signMessage(String message, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        return signature.sign();
    }

    public static boolean verifySignature(String message, byte[] signatureBytes, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey); 
        signature.update(message.getBytes());
        return signature.verify(signatureBytes);
    }

    public static String addSignature(String message, PrivateKey privateKey) throws Exception {
        byte[] signatureBytes = signMessage(message, privateKey);
        String signature = Base64.getEncoder().encodeToString(signatureBytes);
        return message + "@" + signature;
    }

    public static String removeSignature(String signedMessage, PublicKey publicKey) throws Exception {
        int atIndex = signedMessage.lastIndexOf('@');
        if (atIndex == -1) {
            throw new IllegalArgumentException("Invalid message format: no signature found.");
        }

        String message = signedMessage.substring(0, atIndex);
        String signatureBase64 = signedMessage.substring(atIndex + 1);
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

        if (verifySignature(message, signatureBytes, publicKey)) {
            return message;
        } else {
            throw new IllegalArgumentException("Signature verification failed.");
        }
    }

    public static void main(String[] args) throws Exception {
        // Generate key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // You can adjust the key size as needed
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        String message = "Hello, World! ABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String encryptedMessage = encryptAndConcatenate(message, publicKey);
        String signedMessage = addSignature(encryptedMessage, privateKey);
        // transfered over network
        String verifiedMessage = removeSignature(signedMessage, publicKey);
        verifiedMessage = decryptAndConcatenate(verifiedMessage, privateKey);

        System.out.println("Original message: " + message);
        System.out.println("Encrypted message: " + encryptedMessage);
        System.out.println("Signed message: " + signedMessage);
        System.out.println("Verified message: " + verifiedMessage);
    }
}
