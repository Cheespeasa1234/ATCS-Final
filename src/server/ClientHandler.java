package server;

import java.io.*;
import java.net.*;
import java.security.*;

import conn.Packet;
import conn.Security;

class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private PrintWriter out;
    private int clientID;
    private String displayName;

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

                if (packet.getType().equals("LOGON")) {
                    this.displayName = packet.getInfo("username");
                } else {
                    System.err.println("Unknown packet type: " + packet.getType());
                }
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
