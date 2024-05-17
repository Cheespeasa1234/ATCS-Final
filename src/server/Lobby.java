package server;

import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import conn.Packet;

/**
 * The Lobby is the thread that manages the state of the server, and interacts
 * with the clients.
 * It is also responsible for processing packets in the queue and sending
 * responses to the clients.
 */
public class Lobby implements Runnable {

    public enum GameState {
        WAITING, MAKING_PROMPTS, VOTING_PROMPTS, MAKING_PAINTINGS, VOTING_PAINTINGS, GAME_END
    }

    public GameState gameState = GameState.WAITING;
    public ArrayList<Packet.ChatPacketData> chatHistory = new ArrayList<Packet.ChatPacketData>();
    public long nextStateChange;

    // Manage data in and out
    @Override public void run() {
        nextStateChange = System.currentTimeMillis() + 1000;
        while (true) {
            clientLoop: for (int i = 0; i < Server.getClientCount(); i++) {
                ClientHandler client = Server.getClientIndex(i);

                if (client.dataManager == null) {
                    continue clientLoop;
                }

                while (client.dataManager.dataQueue.incomingHasNextPacket()) {
                    Packet packet = client.dataManager.dataQueue.incomingNextPacket();
                    if (packet.type.equals("LOGON")) {
                        String username = packet.logonPacketData.username;
                        client.displayName = username;
                    } else if (packet.type.equals("CHAT")) {
                        System.out.println("Chat packet recieved. Sending now.");
                        broadcast(packet);
                        chatHistory.add(packet.chatPacketData);
                    }
                }
            }

            long currentTime = System.currentTimeMillis();

        }
    }

    private void broadcast(Packet packet) {
        for (int i = 0; i < Server.getClientCount(); i++) {
            ClientHandler client = Server.getClientIndex(i);
            client.dataManager.dataQueue.outgoingAddPacket(packet);
        }
    }
}