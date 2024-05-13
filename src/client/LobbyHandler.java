package client;

import java.io.PrintWriter;
import java.util.ArrayList;

import conn.Packet;

/**
 * The LobbyHandler is the thread that processes the packets.
 */
public class LobbyHandler implements Runnable {
    
    public ArrayList<Packet> packetQueue;
    private PrintWriter out;

    public LobbyHandler(PrintWriter out) {
        this.packetQueue = new ArrayList<Packet>();
        this.out = out;
    }
    
    @Override public void run() {
        while (true) {
            for (Packet packet: this.packetQueue) {
                String type = packet.getType();
                if (type.equals("CHECKUP")) {
                    out.println(packet.toString());
                }
            }
        }
    }
}
