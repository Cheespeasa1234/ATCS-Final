package conn;

import java.io.Serial;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

import server.Lobby;

public class Packet {

    public static class ChatPacketData {
        @SerializedName("content") public String message;
        @SerializedName("sender") public String sender;
        public ChatPacketData(String message, String sender) {
            this.message = message;
            this.sender = sender;
        }
    }
    public static class LogonPacketData {
        @SerializedName("name") public String name;
        public LogonPacketData(String name) {
            this.name = name;
        }
    }
    public static class GameStatePacketData {
        @SerializedName("players") public PlayerLite[] players;
        @SerializedName("chatHistory") public ChatPacketData[] chatHistory;
        @SerializedName("gameState") public Lobby.GameState gameState;
        public GameStatePacketData(PlayerLite[] players, ChatPacketData[] chatHistory, Lobby.GameState gameState) {
            this.players = players;
            this.chatHistory = chatHistory;
            this.gameState = gameState;
        }
    }
    public static class StatusPacketData {
        @SerializedName("status") public PlayerLite.PlayerStatus status;
        public StatusPacketData(PlayerLite.PlayerStatus status) {
            this.status = status;
        }
    }

    @SerializedName("type") public String type; 
    @SerializedName("playerData") public PlayerLite playerData;
    @SerializedName("chatPacketData") public ChatPacketData chatPacketData;
    @SerializedName("logonPacketData") public LogonPacketData logonPacketData;
    @SerializedName("gameStatePacketData") public GameStatePacketData gameStatePacketData;
    @SerializedName("statusPacketData") public StatusPacketData statusPacketData;

    public static Packet playerDataPacket(int clientID, PlayerLite playerLite) {
        Packet packet = new Packet();
        packet.type = "PLAYERDATA";
        packet.playerData = playerLite;
        return packet;
    }

    public static Packet chatPacket(String message, String sender) {
        Packet packet = new Packet();
        packet.type = "CHAT";
        packet.chatPacketData = new ChatPacketData(message, sender);
        return packet;
    }

    public static Packet logonPacket(String name) {
        Packet packet = new Packet();
        packet.type = "LOGON";
        packet.logonPacketData = new LogonPacketData(name);
        return packet;
    }

    public static Packet gameStatePacket(ArrayList<PlayerLiteConn> players, ArrayList<ChatPacketData> chatHistory, Lobby.GameState gameState) {
        Packet packet = new Packet();
        packet.type = "GAMESTATE";
        PlayerLite[] playerArray = players.toArray(new PlayerLite[players.size()]);
        ChatPacketData[] chatArray = chatHistory.toArray(new ChatPacketData[chatHistory.size()]);
        packet.gameStatePacketData = new GameStatePacketData(playerArray, chatArray, gameState);
        return packet;
    }

    public static Packet statusPacket(PlayerLite.PlayerStatus status) {
        Packet packet = new Packet();
        packet.type = "STATUS";
        packet.statusPacketData = new StatusPacketData(status);
        return packet;
    }

    @Override public String toString() {
        return this.toJson().toString();
    }

    public JsonObject toJson() {
        Gson gson = new Gson();
        return gson.toJsonTree(this).getAsJsonObject();
    }

    public static Packet fromJson(String rawJson) {
        Gson gson = new Gson();
        return gson.fromJson(rawJson, Packet.class);
    }
}
