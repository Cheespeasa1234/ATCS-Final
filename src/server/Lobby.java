package server;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import conn.Packet;
import conn.Packet.PacketFactory;

public class Lobby implements Runnable {
    @Override public void run() {

        new Thread(new Runnable() {
            @Override public void run() {
                while (true) {
                    for (ClientHandler client : Server.getClients()) {
                        while (client.packetQueue.outgoingHasNextPacket()) {
                            Packet packet = client.packetQueue.outgoingNextPacket();
                            System.out.println("ClientHandler.java Sent: " + packet.toString());
                            client.out.println(packet.toString());
                        }

                        while (client.packetQueue.incomingHasNextPacket()) {
                            Packet packet = client.packetQueue.incomingNextPacket();
                            System.out.println("ClientHandler.java Recieved: " + packet.toString());

                            if (packet.getType().equals("CHAT")) {
                                packet
                                    .enforcePacketHasProperty("sender")
                                    .enforcePacketHasProperty("content");

                                for (ClientHandler otherClient : Server.getClients()) {
                                    if (otherClient != client) {
                                        otherClient.packetQueue.outgoingAddPacket(packet);
                                    }
                                }
                            } else if (packet.getType().equals("JOINGAME")) {
                                packet
                                    .enforcePacketHasProperty("name")
                                    .enforcePacketHasProperty("");
                            }
                        }
                    }
                }
            }
        }).start();

        while (true)

        {
            // Broadcast message to all connected clients
            for (ClientHandler client : Server.getClients()) {
                Packet packet = new Packet("CHECKUP", Map.of("sent", System.currentTimeMillis() + ""));
                client.packetQueue.outgoingAddPacket(packet);
            }

            try {
                int timeToPause = (int) (Math.random() * 3000.0 + 2000.0); // Sleep for 2-5 seconds
                Thread.sleep(timeToPause);
            } catch (InterruptedException e) {
                System.err.println("Error in lobby: " + e.getMessage());
            }
        }
    }
}