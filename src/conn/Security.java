package conn;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Security {

    /**
     * Turn the base64 form of a RSA public key into a PublicKey object.
     * @param publicKeyStr The public key in string form.
     * @return The PublicKey object.
     */
    public static PublicKey stringToPublicKey(String publicKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    /**
     * Turn the base64 form of a RSA private key into a PrivateKey object.
     * @param privateKeyStr The private key in string form.
     * @return The PrivateKey object.
     */
    public static PrivateKey stringToPrivateKey(String privateKeyStr) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyStr);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    /**
     * Turn the PublicKey object into a base64 form string.
     * @param publicKey the PublicKey object
     * @returns The string form.
     */
    public static String publicKeyToString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Turn the PrivateKey object into a base64 form string.
     * @param privateKey the PublicKey object
     * @returns The string form.
     */
    public static String privateKeyToString(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Encrypt a message with a public key, and return the ciphertext. 
     * The string can not be longer than 245 bytes.
     * @see #encryptAndConcatenate(String, PublicKey)
     * @param message The message to encrypt.
     * @param publicKey The public key to encrypt with.
     * @return The encrypted message in byte form.
     */
    public static String encryptMessage(String message, PublicKey publicKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return new String(cipher.doFinal(message.getBytes()));
    }

    /**
     * Decrypt a message with a private key, and return the plaintext.
     * The encrypted bytes must not include the signature text.
     * To remove a signature in the logon packet, split by @ symbol and remove the second part.
     * Otherwise, use removeSignature.
     * @see #removeSignature(String, PublicKey)
     * @see #decryptAndConcatenate(String, PrivateKey)
     * @param encryptedMessage The encrypted message in byte form.
     * @param privateKey The private key to decrypt with.
     * @return The decrypted message in byte form.
     */
    public static String decryptMessage(String encryptedMessage, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return new String(cipher.doFinal(encryptedMessage.getBytes()));
    }

    /**
     * Encrypt a message with a public key, and concatenate the encrypted chunks with ampersands.
     * The string can be of any length.
     * @param message The message to encrypt.
     * @param publicKey The public key to encrypt with.
     * @return The encrypted message in string form.
     */
    public static String encryptAndConcatenate(String message, PublicKey publicKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        // Split the message into chunks of 245 bytes
        List<byte[]> encryptedChunks = new ArrayList<>();
        int offset = 0;
        while (offset < message.length()) {
            int chunkSize = Math.min(128, message.length() - offset);
            String chunk = message.substring(offset, offset + chunkSize);
            String encryptedChunk = encryptMessage(chunk, publicKey);
            encryptedChunks.add(encryptedChunk.getBytes());
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

    /**
     * Decrypt a message with a private key, and concatenate the decrypted chunks.
     * The encrypted message must be in the form of concatenated chunks with ampersands.
     * @param concatenatedMessage The encrypted message in string form.
     * @param privateKey The private key to decrypt with.
     * @return The decrypted message in string form.
     */
    public static String decryptAndConcatenate(String concatenatedMessage, PrivateKey privateKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        // Split the concatenated message by ampersands
        String[] encryptedChunks = concatenatedMessage.split("&");

        // Decrypt each chunk separately and concatenate them
        StringBuilder decrypted = new StringBuilder();
        for (String encryptedChunk : encryptedChunks) {
            String encryptedBytes = new String(Base64.getDecoder().decode(encryptedChunk));
            String decryptedBytes = decryptMessage(encryptedBytes, privateKey);
            decrypted.append(new String(decryptedBytes));
        }
        return decrypted.toString();
    }

    /**
     * Sign a message with a private key, and return the signature.
     * @param message The message to sign.
     * @param privateKey The private key to sign with.
     * @return The signature in byte form.
     */
    public static String signMessage(String message, PrivateKey privateKey) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes());
        return new String(signature.sign());
    }

    /**
     * Verify a signature with a public key.
     * @param message The message to verify.
     * @param signatureBytes The signature in byte form.
     * @param publicKey The public key to verify with.
     * @return True if the signature is valid, false otherwise.
     */
    public static boolean verifySignature(String message, byte[] signatureBytes, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(message.getBytes());
        return signature.verify(signatureBytes);
    }

    /**
     * Add a signature to a message, and return the signed message.
     * @param message The message to sign.
     * @param privateKey The private key to sign with.
     * @return The signed message in string form.
     */
    public static String addSignature(String message, PrivateKey privateKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        String signatureBytes = signMessage(message, privateKey);
        String signature = Base64.getEncoder().encodeToString(signatureBytes.getBytes());
        return message + "@" + signature;
    }

    /**
     * Remove a signature from a signed message, and return the original message.
     * @param signedMessage The signed message in string form.
     * @param publicKey The public key to verify with.
     * @return The original message in string form.
     */
    public static String removeSignature(String signedMessage, PublicKey publicKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
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

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048); // You can adjust the key size as needed
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        return keyPair;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, SignatureException {
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
