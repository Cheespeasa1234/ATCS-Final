package client;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import javax.crypto.*;

import conn.Packet;

/**
 * The ClientNetworker is the thread that parses and manages authentication and communication with the server.
 * It is responsible for receiving packets from the server, and queuing them for processing.
 */
public class Client {

    private static volatile PrintWriter out;
    private static volatile Queue<Packet> incomingPacketQueue = new LinkedList<Packet>();
    private static volatile Queue<Packet> outgoingPacketQueue = new LinkedList<Packet>();

    private static Thread packetProcessor = new Thread() {
        @Override
        public void run() {
            while (true) {
                if (!incomingPacketQueue.isEmpty()) {
                    Packet packet = incomingPacketQueue.poll();
                    String type = packet.getType();
                    if (type.equals("CHAT")) {
                        String message = packet.getInfo("message");
                        String sender = packet.getInfo("sender");
                        System.out.println(sender + ": " + message);
                    }
                }
                if (!outgoingPacketQueue.isEmpty()) {
                    Packet packet = outgoingPacketQueue.poll();
                    out.println(packet.toString());
                }
            }
        }
    };

    private static Thread chatSender = new Thread() {
        @Override
        public void run() {
            try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
                while (true) {
                    String message = userInput.readLine();
                    Packet packet = new Packet("CHAT", Map.of("message", message, "sender", "Client"));
                    outgoingPacketQueue.add(packet);
                }
            } catch (IOException e) {
                System.err.println("Error reading user input: " + e.getMessage());
            }
        }
    };

    public static void main(String[] args) {
        String serverAddress = "localhost"; // Use localhost to connect to the server running on the same machine
        int port = 58008; // Change this to the port number of your server

        // Generate a key pair for the client
        try (Socket socket = new Socket(serverAddress, port);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter outputWriter = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server on " + serverAddress + ":" + port);
            out = outputWriter;

            // Start the packet processor
            packetProcessor.start();

            // Start the chat sender
            chatSender.start();

            // Read messages from the server and print them
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                Packet packet = new Packet(serverResponse);
                incomingPacketQueue.add(packet);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
