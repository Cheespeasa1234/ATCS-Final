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
            for (ClientHandler client : Server.getClients()) {
                
                if (client.dataManager == null) {
                    continue;
                }

                while (client.dataManager.dataQueue.incomingHasNextPacket()) {
                    JsonObject packet = client.dataManager.dataQueue.incomingNextPacket();
                    System.out.println("ClientHandler.java Recieved: " + packet.toString());

                    String type = packet.get("type").getAsString();
                    if (type.equals("CHAT")) {
                        for (ClientHandler otherClient : Server.getClients()) {
                            otherClient.dataManager.dataQueue.outgoingAddPacket(packet);
                        }
                    } else if (type.equals("JOINGAME")) {

                    }
                }

            }
        }
    }
}