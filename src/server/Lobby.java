package server;

import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * The Lobby is the thread that manages the state of the server, and interacts
 * with the clients.
 * It is also responsible for processing packets in the queue and sending
 * responses to the clients.
 */
public class Lobby implements Runnable {
    @Override public void run() {

        while (true) {
            clientLoop: for (int i = 0; i < Server.getClientCount(); i++) {
                ClientHandler client = Server.getClientIndex(i);

                if (client.dataManager == null) {
                    System.out.println("Skipped");
                    continue clientLoop;
                }

                while (client.dataManager.dataQueue.incomingHasNextPacket()) {
                    JsonObject packet = client.dataManager.dataQueue.incomingNextPacket();
                    System.out.println("ClientHandler.java Recieved: " + packet.toString());

                    String type = packet.get("type").getAsString();
                    if (type.equals("CHAT")) {
                        System.out.println("Chat packet recieved. Sending now.");
                        for (int j = 0; j < Server.getClientCount(); j++) {
                            ClientHandler otherClient = Server.getClientIndex(j);
                            otherClient.dataManager.dataQueue.outgoingAddPacket(packet);
                        }
                    } else if (type.equals("JOINGAME")) {

                    }
                }

            }
        }
    }
}