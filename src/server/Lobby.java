package server;

import java.util.ArrayList;
import java.util.List;

import conn.Packet;
import conn.PlayerLite;
import conn.PlayerLiteConn;
import conn.Utility;
import conn.Utility.Election;
import conn.Packet.ChatPacketData;

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
    private Election currentElection;

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
                    } else if (packet.type.equals(Packet.SubmitPromptPacketData.typeID)) { // Submitted a prompt
                        client.prompt = packet.submitPromptPacketData;
                        broadcastState();
                    } else if (packet.type.equals(Packet.SubmitPaintingPacketData.typeID)) { // Submitted a painting
                        client.painting = packet.submitPaintingPacketData;
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
                } case MAKING_PROMPTS: {
                    break;
                } case VOTING_PAINTINGS: {
                    break;
                } case MAKING_PAINTINGS: {
                    break;
                } case VOTING_PROMPTS: { // People made their prompts, now we vote on which is best
                    if (System.currentTimeMillis() > nextStateChange) {
                        getWinner(currentElection);
                        gameState = Utility.GameState.MAKING_PAINTINGS;
                        nextStateChange = System.currentTimeMillis() + Utility.STATE_INTERVAL;
                        broadcastState();
                    }
                    break;
                } case WAITING: { // Creating prompts phase
                    if (players.size() < 3) { // If the player count is too small again, go back to waiting for players
                        gameState = Utility.GameState.WAITING_FOR_PLAYERS;
                        nextStateChange = System.currentTimeMillis() + Utility.STATE_INTERVAL;
                        broadcastState();
                    } else if (System.currentTimeMillis() > nextStateChange) { // If the waiting time is up, start the next phase
                        
                        // Start the next phase
                        gameState = Utility.GameState.MAKING_PROMPTS;
                        nextStateChange = System.currentTimeMillis() + Utility.STATE_INTERVAL;

                        // Start an election
                        List<String> candidates = new ArrayList<String>();
                        for (PlayerLiteConn player : players) {
                            if (player.prompt != null) {
                                candidates.add(player.prompt.prompt);
                            }
                        }

                        // Tell everyone to vote now
                        this.currentElection = new Election(candidates, "Choose the prompt you want to draw!");
                        broadcast(Packet.startVotePacket(currentElection));
                        broadcastState();
                    } else {} // If every player has submitted a prompt
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

    private void getWinner(Election election) {
        int[] votes = new int[election.candidates.size()];
        for (PlayerLiteConn player : players) {
            if (player.vote != null && player.vote.election == election) {
                votes[player.vote.choice]++;
            }
        }
        int maxVotes = 0;
        int maxIndex = 0;
        for (int i = 0; i < votes.length; i++) {
            if (votes[i] > maxVotes) {
                maxVotes = votes[i];
                maxIndex = i;
            }
        }
        broadcast(Packet.chatPacket(election.candidates.get(maxIndex) + " won the election!", "Server"));
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