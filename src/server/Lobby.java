package server;

import java.util.ArrayList;

import conn.Packet;
import conn.PlayerLite;
import conn.PlayerLiteConn;
import conn.Utility;
import conn.Packet.ChatPacketData;
import conn.Utility.Election;

/**
 * The Lobby is the thread that manages the state of the server, and interacts
 * with the clients.
 * It is also responsible for processing packets in the queue and sending
 * responses to the clients.
 */
public class Lobby implements Runnable {
    private Utility.GameState gameState = Utility.GameState.WAITING_FOR_PLAYERS;
    private long nextStateChange;
    private ArrayList<Packet.ChatPacketData> chatHistory = new ArrayList<Packet.ChatPacketData>();
    private ArrayList<PlayerLiteConn> players = new ArrayList<PlayerLiteConn>();

    // Manage data in and out
    @Override public void run() {
        System.out.println("Lobby running");
        nextStateChange = System.currentTimeMillis() + Utility.STATE_INTERVAL;
        while (true) {

            // First, manage moving data
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
                    if (packet.type.equals(Packet.LogonPacketData.typeID)) { // Logged in
                        client.status = PlayerLite.PlayerStatus.ACTIVE;
                        client.displayName = packet.logonPacketData.name;
                        broadcastState();
                    } else if (packet.type.equals(Packet.ChatPacketData.typeID)) { // Sent a chat message
                        client.status = PlayerLite.PlayerStatus.ACTIVE;
                        addChat(packet.chatPacketData);
                        broadcast(packet);
                        broadcastState();
                    } else if (packet.type.equals(Packet.StatusPacketData.typeID)) { // Sent a status update
                        client.status = packet.statusPacketData.status;    
                        broadcastState();
                    } else if (packet.type.equals(Packet.VotePacketData.typeID)) { // Submitted a vote
                        client.vote = packet.votePacketData;
                        broadcastState();
                    }
                }
            }
            for (int i = 0; i < playersToRemove.size(); i++) {
                System.out.println("Removing " + playersToRemove.get(i).displayName);
                removeClient(playersToRemove.get(i).clientConnection);
            }

            // Next, keep the game moving
            switch (gameState) {
                case GAME_END: { 
                    break;
                } case MAKING_PAINTINGS: {
                    break;
                } case MAKING_PROMPTS: {
                    break;
                } case VOTING_PAINTINGS: {
                    break;
                } case VOTING_PROMPTS: {
                    
                    break;
                } case WAITING: {
                    if (System.currentTimeMillis() > nextStateChange) {
                        
                        // Start the next phase
                        gameState = Utility.GameState.MAKING_PROMPTS;
                        nextStateChange = System.currentTimeMillis() + Utility.STATE_INTERVAL;

                        // Start an election

                        broadcastState();
                    }
                    break;
                } case WAITING_FOR_PLAYERS: { // Waiting for 3 players to join up
                    if (players.size() >= 3) {
                        gameState = Utility.GameState.WAITING;
                        nextStateChange = System.currentTimeMillis() + Utility.STATE_INTERVAL;
                        broadcastState();
                    }
                    break;
                }
            }

        }
    }

    private void addChat(ChatPacketData chatPacketData) {
        chatHistory.add(chatPacketData);
        if (chatHistory.size() > 20) {
            chatHistory.remove(chatHistory.size() - 1);
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
        broadcast(Packet.gameStatePacket(players, chatHistory, gameState, nextStateChange));
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