package server;

import java.io.*;
import java.net.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import conn.Packet;
import conn.Security;

class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private int clientID;
    
    public String displayName;
    public PrintWriter out;
    public long ping;

    public ArrayList<Packet> packetQueue = new ArrayList<Packet>();

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
        while (ServerNetworker.clients.stream().anyMatch(c -> c.clientID == this.clientID)) {
            this.clientID = (int) (Math.random() * 99999);
        }

    }

    public void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
        ServerNetworker.clients.remove(this);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine);
                Packet packet = new Packet(inputLine);
                this.packetQueue.add(packet);
            }

            System.out.println("@" + this.clientID + " disconnected: " + clientSocket.getInetAddress().getHostAddress());
            closeConnection();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("@" + this.clientID + " disconnected: " + clientSocket.getInetAddress().getHostAddress());
            closeConnection();
        }
    }

    public void sendPacket(Packet packet) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        out.println(packet.toString());
    }
}
