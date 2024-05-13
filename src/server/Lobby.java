package server;

import java.util.ArrayList;
import java.util.Map;

import conn.Packet;

/**
 * The Lobby is the thread that manages the state of the server, and interacts with the clients.
 * It is also responsible for processing packets in the queue and sending responses to the clients.
 */
public class Lobby implements Runnable {
    
    private class ChatMessage {
        public String message;
        public String sender;
        public ChatMessage(String message, String sender) {
            this.message = message;
            this.sender = sender;
        }
    }

    private ArrayList<ClientHandler> clients;
    private ArrayList<ChatMessage> chat;

    @Override public void run() {
        clients = new ArrayList<ClientHandler>();
        chat = new ArrayList<ChatMessage>();
        while (true) {
            for (ClientHandler client: clients) {
                for (Packet packet: client.packetQueue) {
                    String type = packet.getType();
                    if (type.equals("CHAT")) {
                        String message = packet.getInfo("message");
                        addChatMessage(message, client.displayName);
                    }
                }
                client.packetQueue.clear();
            }
        }
    }

    public void addChatMessage(String message, String sender) {
        chat.add(new ChatMessage(message, sender));
        for (ClientHandler client: clients) {
            client.out.println(new Packet("CHAT", Map.of("message", message, "sender", sender)).toString());
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
