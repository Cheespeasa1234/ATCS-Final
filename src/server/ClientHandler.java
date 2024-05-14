package server;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.LinkedList;
import java.util.Queue;

import conn.Packet;
import conn.PacketQueue;
import conn.Security;

class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private int clientID;

    public String displayName;
    public PrintWriter out;
    public long ping;

    public PacketQueue packetQueue = new PacketQueue();

    public ClientHandler(Socket clientSocket) {

        // Establish a connection
        this.clientSocket = clientSocket;
        this.ping = -1;
        try {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating PrintWriter: " + e.getMessage());
        }

        // Generate security details
        this.clientID = (int) (Math.random() * 99999);
        while (Server.getClients().stream().anyMatch(c -> c.clientID == this.clientID)) {
            this.clientID = (int) (Math.random() * 99999);
        }

    }

    public void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
        Server.removeClient(this);
    }

    @Override public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                Packet packet = new Packet(inputLine);
                packetQueue.incomingAddPacket(packet);
            }

            System.out.println("Client disconnected: " + clientSocket.getInetAddress().getHostAddress());
            Server.removeClient(this); // Remove this client from the list
            clientSocket.close();
        } catch (IOException e) {
            String message = e.getMessage();
            if (message.equals("Connection reset")) {
                System.out.println(
                        "@" + this.clientID + "disconnected: " + clientSocket.getInetAddress().getHostAddress());
                Server.removeClient(this); // Remove this client from the list
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
