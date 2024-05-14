package server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static List<ClientHandler> clients = new ArrayList<ClientHandler>();
    public static synchronized List<ClientHandler> getClients() {
        return clients;
    }
    public static synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }
    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }


    public static void main(String[] args) {
        // Start the server
        int port = 58008;

        // Start the lobby
        new Thread(new Lobby()).start();

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
    public static void broadcastMessage(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }
}