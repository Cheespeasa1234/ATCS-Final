package client;

import java.io.*;
import java.net.*;

import conn.DataManager;

public class SimpleClient implements Runnable {

    public DataManager dataManager;
    private String address;
    private int port;

    public SimpleClient(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override public void run() {
        try (Socket socket = new Socket(address, port);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server on " + address + ":" + port);
            dataManager = new DataManager(in, out);
            dataManager.dataQueue.incomingAddPacket(dataManager.createPacket("LOGON", "{}"));

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
