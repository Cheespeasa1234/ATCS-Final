package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override public void uncaughtException(Thread t, Throwable e) {
            System.err.println("Unhandled exception in thread: " + t.getName());
            e.printStackTrace(System.err);
        }
    }

    public static void main(String[] args) {

        // Start the server
        int port = 58008;

        // Handle exceptions
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());

        // Start the lobby
        Lobby lobby = new Lobby();
        new Thread(lobby, "Lobby").start();

        // Create a new server socket
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is running on port " + port);
            // Accept incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

                // Create a new ClientHandler for the connected client
                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler, "ClientHandler-" + clientSocket.getInetAddress().getHostAddress()).start();
                lobby.addClient(clientHandler);
            }
        } catch (IOException e) {
            System.err.println("Error connecting new client: " + e.getMessage());
        }
    }
}