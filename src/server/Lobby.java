package server;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import conn.Packet;

public class Lobby implements Runnable {
    @Override
    public void run() {
        while (true) {
            // Broadcast message to all connected clients
            String now = ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
            Packet packet = new Packet("CHECKUP", Map.of("enc", "false", "sent", now), "");
            Server.broadcastMessage(packet.toString());

            try {
                int timeToPause = (int) (Math.random() * 3000.0 + 2000.0); // Sleep for 2-5 seconds
                Thread.sleep(timeToPause);
            } catch (InterruptedException e) {
                System.err.println("Error in lobby: " + e.getMessage());
            }
        }
    }

}