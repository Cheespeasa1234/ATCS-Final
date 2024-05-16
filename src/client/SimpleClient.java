package client;

import java.io.*;
import java.net.*;

import conn.DataManager;

public class SimpleClient implements Runnable {

    public DataManager dataManager;
    private String address;
    private int port;
    private Runnable onStart;

    public SimpleClient(String address, int port, Runnable onStart) {
        this.address = address;
        this.port = port;
        this.onStart = onStart;
    }

    @Override public void run() {
        try (Socket socket = new Socket(address, port);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server on " + address + ":" + port);
            dataManager = new DataManager(in, out);
            dataManager.dataQueue.outgoingAddPacket(dataManager.createPacket("LOGON", "{}"));

            onStart.run();
            while (true) {} // Keep the thread alive
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
