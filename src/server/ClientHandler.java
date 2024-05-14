package server;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.LinkedList;
import java.util.Queue;

import conn.Packet;

/**
 * Thread that handles a single client connection. 
 * It is responsible for managing network and comms.
 * The Lobby is the thread that manages the game state.
 */
public class ClientHandler extends Object implements Runnable {

    public PrintWriter out;
    private Socket clientSocket;
    private int clientID;

    private Queue<Packet> incomingPacketQueue = new LinkedList<Packet>(); // DO NOT MANAGE THIS YOURSELF. USE THE SYNCHRONIZED METHODS BELOW

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
                Packet packet = new Packet(inputLine);
                System.out.println("@" + this.clientID + " says: " + packet.toString());

                // Handle the packet
            }

            System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
            Server.clients.remove(this); // Remove this client from the list
            clientSocket.close();
        } catch (IOException e) {
            String message = e.getMessage();
            if (message.equals("Connection reset")) {
                System.out.println("@" + this.clientID + "disconnected: " + clientSocket.getInetAddress().getHostAddress());
                Server.clients.remove(this); // Remove this client from the list
            } else {
                System.err.println("Error handling client: " + e.getMessage());
            }
        }
    }

    // Send message to the client
    public void sendMessage(String message) {
        out.println(message);
    }
}
