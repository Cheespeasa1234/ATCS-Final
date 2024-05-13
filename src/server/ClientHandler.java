package server;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.time.ZonedDateTime;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import conn.Packet;
import conn.Security;

class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private int clientID;
    
    public String displayName;
    public PrintWriter out;
    public long ping;

    public PublicKey clientPublicKey; // The key used to send messages to the client, and to verify the signature
    public PublicKey serverPublicKey; // The key to send to the client, for them to encrypt messages to the server
    public PrivateKey serverPrivateKey; // The key to decrypt messages from the client, and to sign messages to the client

    public ClientHandler(Socket clientSocket) {

        // Establish a connection
        this.clientSocket = clientSocket;
        this.ping = -1;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating PrintWriter: " + e.getMessage());
        }

        // Generate security details
        this.clientID = (int) (Math.random() * 99999);
        while (Server.clients.stream().anyMatch(c -> c.clientID == this.clientID)) {
            this.clientID = (int) (Math.random() * 99999);
        }

    }

    public void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
        Server.clients.remove(this);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {

                Packet incomingPacket;

                // If it is the logon packet, it doesn't need to be decrypted
                if (inputLine.startsWith("LOGON&")) {
                    incomingPacket = new Packet(inputLine);
                    this.clientPublicKey = Security.stringToPublicKey(incomingPacket.getInfo("pubkey"));

                    // Generate server key pair
                    KeyPair keyPair = Security.generateKeyPair();
                    this.serverPublicKey = keyPair.getPublic();
                    this.serverPrivateKey = keyPair.getPrivate();

                    // Send the server public key to the client
                    Packet packet = new Packet("LOGON", Map.of("pubkey", Security.publicKeyToString(this.serverPublicKey)));
                    String packetString = packet.toString();

                    // Encrypt the packet
                    byte[] encryptedPacket = Security.encryptMessage(packetString, this.clientPublicKey);
                    byte[] signedPacket = Security.signMessage(new String(encryptedPacket), this.serverPrivateKey);

                    // Send the encrypted packet to the client
                    out.println(new String(signedPacket));
                    
                }

                // System.out.println("@" + this.clientID + " says: " + packet.toString());

                // if (packet.getType().equals("LOGON")) {
                //     this.displayName = packet.getInfo("username");
                // } else if (packet.getType().equals("CHECKUP")) {
                //     String startTime = packet.getInfo("sent");
                //     // Convert ISO to epoch
                //     ZonedDateTime zdt = ZonedDateTime.parse(startTime);
                //     long epoch = zdt.toInstant().toEpochMilli();
                //     // Get the time difference
                //     this.ping = System.currentTimeMillis() - epoch;
                // }
            }

            System.out.println("@" + this.clientID + " disconnected: " + clientSocket.getInetAddress().getHostAddress());
            closeConnection();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | SignatureException e) {
            e.printStackTrace();
            System.out.println("@" + this.clientID + " disconnected: " + clientSocket.getInetAddress().getHostAddress());
            closeConnection();
        }
    }

    // Send message to the client
    public void sendMessage(String message) {
        out.println(message);
    }
}
