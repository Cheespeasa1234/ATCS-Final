package client;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Map;

import javax.crypto.*;

import conn.Packet;
import conn.Security;

public class SimpleClient {

    private static PublicKey serverPublicKey; // The key client uses to encrypt messages to the server
    private static PublicKey clientPublicKey; // The key server uses to encrypt messages to the client
    private static PrivateKey clientPrivateKey; // The key client uses to decrypt messages from the server
    
    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String serverAddress = "localhost"; // Use localhost to connect to the server running on the same machine
        int port = 58008; // Change this to the port number of your server

        // Generate a key pair for the client
        KeyPair clientKeyPair = Security.generateKeyPair();
        String clientPublicKey = Security.publicKeyToString(clientKeyPair.getPublic());
        String clientPrivateKey = Security.privateKeyToString(clientKeyPair.getPrivate());

        try (Socket socket = new Socket(serverAddress, port);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server on " + serverAddress + ":" + port);
            Packet logonPacket = new Packet("LOGON", Map.of("pubkey", clientPublicKey));
            out.println(logonPacket.toString());

            // Read messages from the server and print them
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println("Server says: " + serverResponse);

                // Decrypt the message from the server
                String decryptedMessage = new String(Security.decryptMessage(serverResponse.getBytes(), clientKeyPair.getPrivate()));

            }

        } catch (IOException | SignatureException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static Packet handleCheckupPacket(Packet packet) {
        // Send back a checkup packet, with the same headers, but content is the hash of the content
        String hash = packet.toString().hashCode() + "";
        return new Packet("CHECKUP", Map.of("enc", "false", "sent", packet.getInfo("sent"), "content", hash));
    }
}
