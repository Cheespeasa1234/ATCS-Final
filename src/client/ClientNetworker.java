package client;
import java.io.*;
import java.net.*;
import java.security.*;

import java.util.Map;

import javax.crypto.*;

import conn.Packet;

public class ClientNetworker {

    private static PrintWriter out;
    private static LobbyHandler lobbyHandler;

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String serverAddress = "localhost"; // Use localhost to connect to the server running on the same machine
        int port = 58008; // Change this to the port number of your server

        // Generate a key pair for the client
        try (Socket socket = new Socket(serverAddress, port);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server on " + serverAddress + ":" + port);
            out = outputWriter;

            // Start the lobby handler
            lobbyHandler = new LobbyHandler(out);
            new Thread(lobbyHandler).start();

            // Read messages from the server and print them
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                Packet packet = new Packet(serverResponse);
                lobbyHandler.packetQueue.add(packet);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void sendPacket(Packet packet) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        out.println(packet.toString());
    }
}
