package server;

import java.util.ArrayList;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import conn.Packet;
import conn.PlayerLite;
import conn.PlayerLiteConn;

/**
 * The Lobby is the thread that manages the state of the server, and interacts
 * with the clients.
 * It is also responsible for processing packets in the queue and sending
 * responses to the clients.
 */
public class Lobby implements Runnable {

    public static enum GameState {
        WAITING, MAKING_PROMPTS, VOTING_PROMPTS, MAKING_PAINTINGS, VOTING_PAINTINGS, GAME_END
    }

    public GameState gameState = GameState.WAITING;
    public ArrayList<Packet.ChatPacketData> chatHistory = new ArrayList<Packet.ChatPacketData>();
    public ArrayList<PlayerLiteConn> players = new ArrayList<PlayerLiteConn>();
    public long nextStateChange;

    // Manage data in and out
    @Override public void run() {
        System.out.println("Lobby running");
        nextStateChange = System.currentTimeMillis() + 1000;
        while (true) {
            ArrayList<PlayerLiteConn> clients = this.getClients();
            ArrayList<PlayerLiteConn> playersToRemove = new ArrayList<PlayerLiteConn>();
            for (PlayerLiteConn client : clients) {
                
                if (client.clientConnection.dataManager == null) {
                    continue;
                }

                if (client.clientConnection.closeme) {
                    System.out.println("Client killed himself");
                    playersToRemove.add(client);
                    continue;
                }
                

                while (client.clientConnection.dataManager.dataQueue.incomingHasNextPacket()) {
                    Packet packet = client.clientConnection.dataManager.dataQueue.incomingNextPacket();
                    if (packet.type.equals("LOGON")) {
                        client.status = PlayerLite.PlayerStatus.ACTIVE;
                        client.displayName = packet.logonPacketData.name;
                        
                        broadcastState();
                    } else if (packet.type.equals("CHAT")) {
                        client.status = PlayerLite.PlayerStatus.ACTIVE;
                        
                        chatHistory.add(packet.chatPacketData);
                        if (chatHistory.size() > 20) {
                            chatHistory.remove(chatHistory.size() - 1);
                        }
                        broadcast(packet);
                        broadcastState();
                    } else if (packet.type.equals("STATUS")) {
                        client.status = packet.statusPacketData.status;
                        
                        broadcastState();
                    }
                }
            }

            for (int i = 0; i < playersToRemove.size(); i++) {
                System.out.println("Removing " + playersToRemove.get(i).displayName);
                removeClient(playersToRemove.get(i).clientConnection);
            }

        }
    }

    private void broadcast(Packet packet) {
        System.out.println("Broadcasting packet: " + packet.toString());
        for (PlayerLiteConn player : players) {
            if (player.clientConnection.dataManager != null) {
                player.clientConnection.dataManager.dataQueue.outgoingAddPacket(packet);
            }
        }
    }

    private void broadcastState() {
        broadcast(Packet.gameStatePacket(players, chatHistory, gameState));
    }

    public synchronized void addClient(ClientHandler clientHandler) {
        System.out.println("adding client");
        // Add the client
        PlayerLiteConn conn = new PlayerLiteConn(clientHandler.clientSocket.getInetAddress().getHostAddress());
        conn.clientConnection = clientHandler;
        this.players.add(conn);
    }

    private synchronized void removeClient(ClientHandler clientHandler) {
        // Find the client with that clientHandler
        for (int i = 0; i < this.players.size(); i++) {
            if (this.players.get(i).clientConnection == clientHandler) {
                this.players.remove(i);
                clientHandler.closeConnection();
                broadcastState();
                return;
            }
        }
        // THIS LINE SHOULD HOPEFULLY NEVER BE RUN
    }

    public synchronized ArrayList<PlayerLiteConn> getClients() {
        return this.players;
    }
}