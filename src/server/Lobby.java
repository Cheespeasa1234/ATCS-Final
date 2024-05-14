package server;

import java.util.ArrayList;
import java.util.Map;

import conn.Packet;

/**
 * The Lobby is the thread that manages the state of the server, and interacts with the clients.
 * It is also responsible for processing packets in the queue and sending responses to the clients.
 */
public class Lobby implements Runnable {
    
    private volatile ArrayList<ClientHandler> clients;

    @Override public void run() {
        clients = new ArrayList<ClientHandler>();
        while (true) {
            for (ClientHandler client: clients) {
                while(!client.incomingPacketQueue.isEmpty()) {
                    Packet packet = client.incomingPacketQueue.poll();
                    String type = packet.getType();
                    if (type.equals("CHAT")) {
                        String message = packet.getInfo("message");
                        addChatMessage(message, client.displayName);
                    }
                }
            }
        }
    }

    public void addChatMessage(String message, String sender) {
        for (ClientHandler client: clients) {
            client.outgoingPacketQueue.add(new Packet("CHAT", Map.of("message", message, "sender", sender)));
        }
    }

    public void addClient(ClientHandler client) {
        clients.add(client);
        addChatMessage(client.displayName + " has joined the lobby.", "Server");
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        addChatMessage(client.displayName + " has left the lobby.", "Server");
    }
}
