package client;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Map;

import javax.crypto.*;

import conn.Packet;
import conn.PacketQueue;

public class SimpleClient implements Runnable {

    public PacketQueue packetQueue = new PacketQueue();
    private String address;
    private int port;
    private PrintWriter out;

    public SimpleClient(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override public void run() {
        try (Socket socket = new Socket(address, port);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            this.out = out;

            System.out.println("Connected to server on " + address + ":" + port);
            Packet logonPacket = new Packet("LOGON", Map.of());
            out.println(logonPacket.toString());

            // Read messages from the server and print them
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                Packet packet = new Packet(serverResponse);
                packetQueue.incomingAddPacket(packet);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void sendPacket(Packet packet) {
        out.println(packet);
    }
}
