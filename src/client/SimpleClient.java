package client;

import java.io.*;
import java.net.*;

import conn.DataManager;
import conn.Packet;

public class SimpleClient implements Runnable {

    public DataManager dataManager;
    private String address;
    private int port;
    private String username;
    private Runnable onStart;

    public SimpleClient(String address, int port, String username, Runnable onStart) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.onStart = onStart;
    }

    @Override public void run() {
        try {
            Socket socket = new Socket(address, port);
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to server on " + address + ":" + port);
            dataManager = new DataManager(in, out);
            Packet packet = Packet.logonPacket(username);
            System.out.println("Sending logon packet: " + packet.toJson().toString());
            dataManager.dataQueue.outgoingAddPacket(packet);

            onStart.run();
            while (true) {} // Keep the thread alive
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
