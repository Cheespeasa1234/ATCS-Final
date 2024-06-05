package server;

import java.util.ArrayList;
import java.util.List;

import conn.Packet;
import conn.PlayerLite;
import conn.PlayerLiteConn;
import conn.Utility;
import conn.Utility.Election;
import conn.Packet.ChatPacketData;
import conn.Packet.SubmitPaintingPacketData;
import conn.Packet.SubmitPromptPacketData;

/**
 * The Lobby is the thread that manages the state of the server, and interacts
 * with the clients. It is also responsible for processing packets in the queue
 * and sending responses to the clients.
 */
public class Lobby implements Runnable {
    public Utility.GameState gameState = Utility.GameState.WAITING_FOR_PLAYERS;
    public long nextStateChange;
    public int stateChangeDelay = 0;
    public ArrayList<Packet.ChatPacketData> chatHistory = new ArrayList<Packet.ChatPacketData>();
    public ArrayList<PlayerLiteConn> players = new ArrayList<PlayerLiteConn>();
    public Election currentElection;

    private void resetGame() {
        gameState = Utility.GameState.WAITING_FOR_PLAYERS;
        nextStateChange = System.currentTimeMillis() + Utility.SECONDS_30;
        currentElection = null;
        for (PlayerLiteConn player : this.getClients()) {
            player.resetGame();
        }
    }

    // Manage data in and out
    @Override
    public void run() {
        System.out.println("Lobby running");
        Thread pingThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    broadcast(Packet.pingPacket(System.currentTimeMillis(), 0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        pingThread.start();
        nextStateChange = System.currentTimeMillis() + Utility.SECONDS_30;
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
                        broadcastInfoMessage(client.displayName + " has joined the game.");
                        broadcastState();
                    } else if (packet.type.equals(Packet.ChatPacketData.typeID)) { // Sent a chat message
                        client.status = PlayerLite.PlayerStatus.ACTIVE;
                        String msg = packet.chatPacketData.message;
                        if (msg.isEmpty() || msg.isBlank()) {
                            continue;
                        } else if (msg.startsWith("//")) {
                            if (msg.equalsIgnoreCase("//players")) {
                                broadcastInfoMessage("Players in the game:");
                                for (PlayerLiteConn player : players) {
                                    broadcastInfoMessage(Packet.playerDataPacket(player).toString());
                                }
                                broadcastState();
                            } else if (msg.equalsIgnoreCase("//reset")) {
                                broadcastInfoMessage("Resetting game.");
                                resetGame();
                                broadcastState();
                            }
                        } else {
                            addChat(packet.chatPacketData);
                            broadcast(packet);
                            broadcastState();
                        }
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
            }
            case MAKING_PROMPTS: {
                break;
            }
            case VOTING_PAINTINGS: {
                break;
            }
            case MAKING_PAINTINGS: {
                // People need to submit their paintings
                if (System.currentTimeMillis() > nextStateChange) {
                    // Make sure at least half of the people have submitted paintings
                    int numPaintings = 0;
                    for (PlayerLiteConn player : players) {
                        if (player.painting != null) {
                            numPaintings++;
                        }
                    }
                    if (numPaintings < players.size() / 2) {
                        delayState(Utility.SECONDS_30);
                        break;
                    }
                    gameState = Utility.GameState.VOTING_PAINTINGS;
                    delayState(Utility.SECONDS_60);
                    // Start an election
                    List<SubmitPaintingPacketData> candidates = new ArrayList<SubmitPaintingPacketData>();
                    for (PlayerLiteConn player : players) {
                        if (player.painting != null) {
                            candidates.add(player.painting);
                        }
                    }
                    // Tell everyone to vote now
                    this.currentElection = new Election<SubmitPaintingPacketData>(candidates,
                            "Choose the painting you like the most!");
                    broadcast(Packet.startVotePacket(currentElection));
                    broadcastState();
                }
                break;
            }
            case VOTING_PROMPTS: { // People made their prompts, now we vote on which is best
                if (System.currentTimeMillis() > nextStateChange) {
                    SubmitPromptPacketData winner = (SubmitPromptPacketData) currentElection.getWinner(); // Get the
                                                                                                          // prompt
                                                                                                          // people want
                                                                                                          // to draw
                    System.out.println("Winner is: " + winner.prompt);
                    gameState = Utility.GameState.MAKING_PAINTINGS;
                    delayState(Utility.SECONDS_60);
                    broadcastState();
                }
                break;
            }
            case WAITING: { // Creating prompts phase
                if (players.size() < Utility.PLAYERS_NEEDED) { // If the player count is too small again, go back to
                                                               // waiting for players
                    gameState = Utility.GameState.WAITING_FOR_PLAYERS;
                    delayState(Utility.SECONDS_30);
                    broadcastInfoMessage("Waiting for " + (3 - players.size()) + " more players to join.");
                    broadcastState();
                } else if (System.currentTimeMillis() > nextStateChange) { // If the waiting time is up, start the next
                                                                           // phase
                    // Make sure at least half of the people have submitted prompts
                    int numPrompts = 0;
                    for (PlayerLiteConn player : players) {
                        if (player.prompt != null) {
                            numPrompts++;
                        }
                    }
                    System.out.println("Num prompts: " + numPrompts);
                    if (numPrompts < players.size() / 2) {
                        delayState(Utility.SECONDS_30);
                        broadcastInfoMessage("Waiting for " + (players.size() - numPrompts)
                                + " more players to submit their prompts.");
                        break;
                    }
                    // Start the next phase
                    gameState = Utility.GameState.MAKING_PROMPTS;
                    delayState(Utility.SECONDS_60);
                    // Start an election
                    List<SubmitPromptPacketData> candidates = new ArrayList<SubmitPromptPacketData>();
                    for (PlayerLiteConn player : players) {
                        if (player.prompt != null) {
                            candidates.add(player.prompt);
                        }
                    }
                    // Tell everyone to vote now
                    this.currentElection = new Election<SubmitPromptPacketData>(candidates,
                            "Choose the prompt you want to draw!");
                    broadcast(Packet.startVotePacket(currentElection));
                    broadcastState();
                } else {
                } // If every player has submitted a prompt
                break;
            }
            case WAITING_FOR_PLAYERS: { // Waiting for 3 players to join up
                if (players.size() >= Utility.PLAYERS_NEEDED) {
                    gameState = Utility.GameState.WAITING;
                    broadcastInfoMessage("Game is starting.");
                    broadcastState();
                } else {
                    delayState(Utility.SECONDS_30);
                }
                break;
            }
            }
        }
    }

    private void delayState(int ms) {
        nextStateChange = System.currentTimeMillis() + ms;
        stateChangeDelay = ms;
    }

    private void addChat(ChatPacketData chatPacketData) {
        chatHistory.add(chatPacketData);
        if (chatHistory.size() > 20) {
            chatHistory.remove(chatHistory.size() - 1);
        }
    }

    private void broadcast(Packet packet) {
        for (PlayerLiteConn player : players) {
            if (player.clientConnection.dataManager != null) {
                player.clientConnection.dataManager.dataQueue.outgoingAddPacket(packet);
            }
        }
    }

    private void broadcastState() {
        // Broadcast Gamestate
        broadcast(Packet.gameStatePacket(this));
        // Give each player their own status
        for (PlayerLiteConn player : players) {
            if (player.clientConnection.dataManager != null) {
                player.clientConnection.dataManager.dataQueue.outgoingAddPacket(Packet.playerDataPacket(player));
            }
        }
    }

    private void broadcastInfoMessage(String message) {
        broadcast(Packet.serverMessagePacket(message));
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
                broadcastInfoMessage(clientHandler.displayName + " has left the game.");
                broadcastState();
                return;
            }
        }
        // THIS LINE SHOULD HOPEFULLY NEVER BE RUN
    }

    public synchronized ArrayList<PlayerLiteConn> getClients() {
        // return a shallow copy of the clients
        return new ArrayList<PlayerLiteConn>(this.players);
    }
}