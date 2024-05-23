package server;

import java.io.*;
import java.net.*;

import conn.Utility;

public class Server {

    public static void main(String[] args) {

        // Start the server
        int port = 58008;

        // Handle exceptions
        Thread.setDefaultUncaughtExceptionHandler(new Utility.GlobalExceptionHandler());

        // Start the lobby
        Lobby lobby = new Lobby();
        Thread lobbyThread = new Thread(lobby);
        lobbyThread.setUncaughtExceptionHandler(new Utility.GlobalExceptionHandler());
        lobbyThread.setName("Lobby");
        lobbyThread.start();

        // Create a new server socket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);
            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new ClientHandler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                Thread clientThread = new Thread(clientHandler);
                clientThread.setUncaughtExceptionHandler(new Utility.GlobalExceptionHandler());
                clientThread.setName("ClientHandler-" + clientSocket.getInetAddress().getHostAddress());
                clientThread.start();
                
                lobby.addClient(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Error connecting new client: " + e.getMessage());
        }
    }
}