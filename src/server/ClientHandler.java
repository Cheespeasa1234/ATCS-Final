package server;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.LinkedList;
import java.util.Queue;

import conn.DataManager;
import conn.Security;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The ClientHandler is the thread that manages communication with a single client.
 * It is responsible for receiving packets from the client, and queuing them for processing.
 */
public class ClientHandler implements Runnable {

    public final Socket clientSocket;
    public int clientID;

    public String displayName;
    public PrintWriter out;

    public DataManager dataManager;
    public boolean closeme = false;

    public ClientHandler(Socket clientSocket) {

        // Establish a connection
        this.clientSocket = clientSocket;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating PrintWriter: " + e.getMessage());
        }

        // Generate security details
        this.clientID = (int) (Math.random() * 99999);
    }

    /**
     * Terminates the connection with the client.
     */
    public void closeConnection() {
        System.out.println("@" + this.clientID + " disconnected: " + clientSocket.getInetAddress().getHostAddress());
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
        dataManager.running = false;
        closeme = true;
        running = false;
    }

    // Declare a volatile boolean flag to control the loop
    private volatile boolean running = true;

    @Override public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            dataManager = new DataManager(in, out, clientSocket.getInetAddress().getHostAddress());
            dataManager.addInputTerminationEvent(() -> {
                this.closeConnection();
                running = false; // Set the flag to false when termination event occurs
            });

            // Loop while the flag is true
            while (running) {
                // Add a small sleep to prevent busy-waiting
                Thread.sleep(100);
            }
            System.out.println("@" + this.clientID + " thread terminated");
        } catch (IOException e) {
            String message = e.getMessage();
            if (message.equals("Connection reset")) {
                System.out.println(
                        "@" + this.clientID + "disconnected because connection reset: "
                                + clientSocket.getInetAddress().getHostAddress());
            } else {
                System.err.println("Error handling client: " + e.getMessage());
            }
        } catch (InterruptedException e) {
            // Handle interruption
            Thread.currentThread().interrupt();
            System.err.println("Thread was interrupted: " + e.getMessage());
        }
    }

}
