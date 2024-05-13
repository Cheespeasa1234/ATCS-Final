package client;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import conn.Packet;

/**
 * The LobbyHandler is the thread that processes the packets in the packet queue.
 * It is responsible for sending responses to the server and managing state.
 */
public class LobbyHandler implements Runnable {
    
    public ArrayList<Packet> packetQueue;
    private PrintWriter out;

    public LobbyHandler(PrintWriter out) {
        this.packetQueue = new ArrayList<Packet>();
        this.out = out;
    }
    
    @Override public void run() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                Packet packet = new Packet("CHAT", Map.of("message", message, "sender", "Client"));
                this.packetQueue.add(packet);
            }
        }).start();

        while (true) {
            for (Packet packet: this.packetQueue) {
                String type = packet.getType();
                if (type.equals("CHECKUP")) {
                    out.println(packet.toString());
                } else if (type.equals("CHAT")) {
                    String message = packet.getInfo("message");
                    String sender = packet.getInfo("sender");
                    System.out.println(sender + " says: " + message);
                }
            }
            packetQueue.clear();
        }
    }
}
