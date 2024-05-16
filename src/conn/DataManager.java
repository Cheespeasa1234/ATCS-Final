package conn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

public class DataManager {

    public DataQueue dataQueue;
    public Gson gson;

    public class DataQueue {
        private Queue<JsonObject> incomingPacketQueue = new LinkedList<JsonObject>();
        private Queue<JsonObject> outgoingPacketQueue = new LinkedList<JsonObject>();

        public synchronized boolean incomingHasNextPacket() {
            return !incomingPacketQueue.isEmpty();
        }

        public synchronized JsonObject incomingNextPacket() {
            return incomingPacketQueue.poll();
        }

        public synchronized void incomingAddPacket(JsonObject packet) {
            incomingPacketQueue.add(packet);
        }

        public synchronized boolean outgoingHasNextPacket() {
            return !outgoingPacketQueue.isEmpty();
        }

        public synchronized JsonObject outgoingNextPacket() {
            return outgoingPacketQueue.poll();
        }

        public synchronized void outgoingAddPacket(JsonObject packet) {
            outgoingPacketQueue.add(packet);
        }
    }

    private Runnable inputTerminationEvent;

    public void addInputTerminationEvent(Runnable r) {
        this.inputTerminationEvent = r;
    }

    public DataManager(BufferedReader in, PrintWriter out) {
        gson = new Gson();
        dataQueue = new DataQueue();

        // Input thread
        new Thread(() -> {
            String response;
            try {
                while ((response = in.readLine()) != null) {
                    System.out.println("Recieved: '" + response + "'");
                    if (!response.equals("null")) {
                        JsonObject packet = gson.fromJson(response, JsonObject.class);
                        dataQueue.incomingAddPacket(packet);
                    }
                }
                if (inputTerminationEvent != null) {
                    inputTerminationEvent.run();
                }
            } catch (JsonSyntaxException | IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Output thread
        new Thread(() -> {
            while (true) {
                if (dataQueue.outgoingHasNextPacket()) {
                    JsonObject packet = dataQueue.outgoingNextPacket();
                    System.out.println("Sending: " + packet.toString());
                    out.println(packet);
                }
            }
        }).start();
    }

    public JsonObject createPacket(String type, String jsonData) {
        JsonObject data = gson.fromJson(jsonData, JsonObject.class);
        return gson.fromJson("{ \"type\": \"" + type + "\", \"data\": " + data + " }", JsonObject.class);
    }

    public static boolean packetDataHasKeys(JsonObject packet, String[] keys) {
        for (String key : keys) {
            if (!packet.get("data").getAsJsonObject().has(key)) {
                return false;
            }
        }
        return true;
    }
}
