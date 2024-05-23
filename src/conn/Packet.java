package conn;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import conn.Utility.Election;

import java.util.List;

public class Packet {

    public static class ChatPacketData {
        @SerializedName("content") public String message;
        @SerializedName("sender") public String sender;
        public static transient final String typeID = "CHAT";

        public ChatPacketData(String message, String sender) {
            this.message = message;
            this.sender = sender;
        }
    }

    public static class LogonPacketData {
        @SerializedName("name") public String name;
        public static transient final String typeID = "LOGON";

        public LogonPacketData(String name) {
            this.name = name;
        }
    }

    public static class GameStatePacketData {
        @SerializedName("players") public PlayerLite[] players;
        @SerializedName("chatHistory") public ChatPacketData[] chatHistory;
        @SerializedName("gameState") public Utility.GameState gameState;
        @SerializedName("nextStateChange") public long nextStateChange;
        public static transient final String typeID = "GAMESTATE";

        public GameStatePacketData(PlayerLite[] players, ChatPacketData[] chatHistory, Utility.GameState gameState,
                long nextStateChange) {
            this.players = players;
            this.chatHistory = chatHistory;
            this.gameState = gameState;
            this.nextStateChange = nextStateChange;
        }
    }

    public static class StatusPacketData {
        @SerializedName("status") public PlayerLite.PlayerStatus status;
        public static transient final String typeID = "STATUS";

        public StatusPacketData(PlayerLite.PlayerStatus status) {
            this.status = status;
        }
    }

    public static class VotePacketData {
        @SerializedName("election") public Election election;
        @SerializedName("choice") public int choice;
        public static transient final String typeID = "VOTE";

        public VotePacketData(Election election, int choice) {
            this.election = election;
            this.choice = choice;
        }
    }

    @SerializedName("type") public String type;
    @SerializedName("playerData") public PlayerLite playerData;
    @SerializedName("chatPacketData") public ChatPacketData chatPacketData;
    @SerializedName("logonPacketData") public LogonPacketData logonPacketData;
    @SerializedName("gameStatePacketData") public GameStatePacketData gameStatePacketData;
    @SerializedName("statusPacketData") public StatusPacketData statusPacketData;
    @SerializedName("votePacketData") public VotePacketData votePacketData;

    public static Packet playerDataPacket(int clientID, PlayerLite playerLite) {
        Packet packet = new Packet();

        packet.playerData = playerLite;
        return packet;
    }

    public static Packet chatPacket(String message, String sender) {
        Packet packet = new Packet();
        packet.type = ChatPacketData.typeID;
        packet.chatPacketData = new ChatPacketData(message, sender);
        return packet;
    }

    public static Packet logonPacket(String name) {
        Packet packet = new Packet();
        packet.type = LogonPacketData.typeID;
        packet.logonPacketData = new LogonPacketData(name);
        return packet;
    }

    public static Packet gameStatePacket(List<PlayerLiteConn> players, List<ChatPacketData> chatHistory,
            Utility.GameState gameState, long nextStateChange) {
        Packet packet = new Packet();
        packet.type = GameStatePacketData.typeID;
        PlayerLite[] playerArray = players.toArray(new PlayerLite[players.size()]);
        ChatPacketData[] chatArray = chatHistory.toArray(new ChatPacketData[chatHistory.size()]);
        packet.gameStatePacketData = new GameStatePacketData(playerArray, chatArray, gameState, nextStateChange);
        return packet;
    }

    public static Packet statusPacket(PlayerLite.PlayerStatus status) {
        Packet packet = new Packet();
        packet.type = StatusPacketData.typeID;
        packet.statusPacketData = new StatusPacketData(status);
        return packet;
    }

    public static Packet votePacket(Election election, int choice) {
        Packet packet = new Packet();
        packet.type = VotePacketData.typeID;
        packet.votePacketData = new VotePacketData(election, choice);
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
