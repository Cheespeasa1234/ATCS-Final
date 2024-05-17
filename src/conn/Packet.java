package conn;

import java.io.Serial;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Packet {

    public static class PlayerData {
        @SerializedName("displayName") public String displayName;
        @SerializedName("clientID") public int clientID;
        public PlayerData(String displayName, int clientID) {
            this.displayName = displayName;
            this.clientID = clientID;
        }
    }
    public static class ChatPacketData {
        @SerializedName("content") public String message;
        @SerializedName("sender") public String sender;
        public ChatPacketData(String message, String sender) {
            this.message = message;
            this.sender = sender;
        }
    }
    public static class LogonPacketData {
        @SerializedName("user") public String username;
        public LogonPacketData(String username) {
            this.username = username;
        }
    }
    public static class GameStatePacketData {
        @SerializedName("players") public PlayerData[] players;
        @SerializedName("chatHistory") public ChatPacketData[] chatHistory;
        @SerializedName("gameState") public String gameState;
        public GameStatePacketData(PlayerData[] players, ChatPacketData[] chatHistory, String gameState) {
            this.players = players;
            this.chatHistory = chatHistory;
            this.gameState = gameState;
        }
    }

    @SerializedName("type") public String type; 
    @SerializedName("playerData") public PlayerData playerData;
    @SerializedName("chatPacketData") public ChatPacketData chatPacketData;
    @SerializedName("logonPacketData") public LogonPacketData logonPacketData;
    @SerializedName("gameStatePacketData") public GameStatePacketData gameStatePacketData;

    public static Packet playerDataPacket(String displayName, int clientID) {
        Packet packet = new Packet();
        packet.type = "PLAYERDATA";
        packet.playerData = new PlayerData(displayName, clientID);
        return packet;
    }

    public static Packet chatPacket(String message, String sender) {
        Packet packet = new Packet();
        packet.type = "CHAT";
        packet.chatPacketData = new ChatPacketData(message, sender);
        return packet;
    }

    public static Packet logonPacket(String username) {
        Packet packet = new Packet();
        packet.type = "LOGON";
        packet.logonPacketData = new LogonPacketData(username);
        return packet;
    }

    public static Packet gameStatePacket(PlayerData[] players, ChatPacketData[] chatHistory, String gameState) {
        Packet packet = new Packet();
        packet.type = "GAMESTATE";
        packet.gameStatePacketData = new GameStatePacketData(players, chatHistory, gameState);
        return packet;
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
