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
            }
        } catch (IOException e) {
            System.err.println("Error connecting new client: " + e.getMessage());
        }
    }
}