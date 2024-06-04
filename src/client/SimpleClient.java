package client;

import java.io.*;
import java.net.*;

import javax.swing.JOptionPane;
import javax.swing.text.Utilities;

import conn.DataManager;
import conn.Packet;
import conn.Utility;

/**
 * The manager of the connection to the server, client-side.
 * It manages the data incoming and outgoing, events, and connection info.
 */
public class SimpleClient implements Runnable {

    public DataManager dataManager;
    private String address;
    private int port;
    private Runnable onStart;
    private String username;

    public SimpleClient(String address, int port, String username, Runnable onStart) {
        this.address = address;
        this.port = port;
        this.username = username;
        this.onStart = onStart;
    }

    @Override public void run() {
        try {
            
            // Get a connection to the server
            Socket socket = new Socket(address, port);
            socket.setSoTimeout(3000); // Set 3 second timeout
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send my username
            System.out.println("Connected to server on " + address + ":" + port);
            dataManager = new DataManager(in, out, "SimpleClient");
            dataManager.addInputTerminationEvent(() -> {
                System.out.println("Connection terminated");
                System.exit(0);
            });
            Packet packet = Packet.logonPacket(this.username);
            System.out.println("Sending logon packet: " + packet.toJson().toString());
            dataManager.dataQueue.outgoingAddPacket(packet);

            onStart.run();
            
            // Keep the thread alive
            while (true) {}
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, Utility.getStackTraceString(e), e.toString(), JOptionPane.ERROR_MESSAGE);
        }
    }
}
