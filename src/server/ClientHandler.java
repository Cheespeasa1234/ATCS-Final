package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import conn.Packet;

/**
 * The ClientHandler is the thread that manages communication with a single client.
 * It is responsible for receiving packets from the client, and queuing them for processing.
 */
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private int clientID;
    
    public String displayName;
    public PrintWriter out;
    public long ping;

    public ArrayList<Packet> packetQueue;

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
        while (Networker.clients.stream().anyMatch(c -> c.clientID == this.clientID)) {
            this.clientID = (int) (Math.random() * 99999);
        }

        packetQueue = new ArrayList<Packet>();

    }

    /**
     * Terminates the connection with the client.
     */
    public void closeConnection() {
        System.out.println("@" + this.clientID + " disconnected: " + clientSocket.getInetAddress().getHostAddress());
        Networker.clients.remove(this);
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            
            // Read messages from the client
            // TODO: Implement packet handling
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                Packet packet = new Packet(inputLine);
                this.packetQueue.add(packet);
            }

            // This loop will exit when the client disconnects
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
            closeConnection();
        }
    }

}
