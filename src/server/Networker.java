package server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The ServerNetworker is the main class of the server.
 * It is responsible for connecting new clients and allocating them to ClientHandlers.
 */
public class Networker {
    public static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) {
        
        // Start the server
        int port = 58008;

        // Start the lobby
        Lobby lobby = new Lobby();
        new Thread(lobby).start();

        // Create a new server socket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);
            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                
                // Create a new ClientHandler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler); // Add to the list of connected clients
                new Thread(clientHandler).start();

                // Connect the client to the lobby
                lobby.addClient(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Error connecting new client: " + e.getMessage());
        }
    }
}