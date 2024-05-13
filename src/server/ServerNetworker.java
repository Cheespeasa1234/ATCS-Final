package server;
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import conn.Packet;

public class ServerNetworker {
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        // Start the server
        int port = 58008;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                // Create a new ClientHandler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler); // Add to the list of connected clients
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            System.err.println("Error in server: " + e.getMessage());
        }
    }

    // Broadcast message to all connected clients
    public static void broadcastMessage(String message) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        for (ClientHandler client : clients) {
            client.sendPacket(new Packet("MESSAGE", Map.of("message", message)));
        }
    }
}