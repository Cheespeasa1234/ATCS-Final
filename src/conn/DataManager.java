package conn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class DataManager {

    public DataQueue dataQueue;
    public Gson gson;

    public class DataQueue {
        private Queue<Packet> incomingPacketQueue = new LinkedList<Packet>();
        private Queue<Packet> outgoingPacketQueue = new LinkedList<Packet>();

        public synchronized boolean incomingHasNextPacket() {
            return !incomingPacketQueue.isEmpty();
        }

        public synchronized Packet incomingNextPacket() {
            return incomingPacketQueue.poll();
        }

        public synchronized void incomingAddPacket(Packet packet) {
            incomingPacketQueue.add(packet);
        }

        public synchronized boolean outgoingHasNextPacket() {
            return !outgoingPacketQueue.isEmpty();
        }

        public synchronized Packet outgoingNextPacket() {
            return outgoingPacketQueue.poll();
        }

        public synchronized void outgoingAddPacket(Packet packet) {
            outgoingPacketQueue.add(packet);
        }
    }

    private Runnable inputTerminationEvent;

    public void addInputTerminationEvent(Runnable r) {
        this.inputTerminationEvent = r;
    }

    public volatile boolean running = true;

    public DataManager(BufferedReader in, PrintWriter out, String threadNameFormat) {
        gson = new Gson();
        dataQueue = new DataQueue();

        // Input thread
        Thread inputThread = new Thread(() -> {
            while (running) {
                String response;
                try {
                    while ((response = in.readLine()) != null) {
                        if (!response.equals("null")) {
                            Packet p = Packet.fromJson(response);
                            dataQueue.incomingAddPacket(p);
                            System.out.println("Received: " + p.toString());
                        }
                    }
                    System.out.println("Connection terminated.");
                    if (inputTerminationEvent != null) {
                        inputTerminationEvent.run();
                    }
                } catch (JsonSyntaxException | IOException e) {
                    e.printStackTrace();
                    running = false;
                    throw new RuntimeException("Connection terminated: " + e.getMessage());
                }
            }
        });
        inputThread.setUncaughtExceptionHandler(new Utility.GlobalExceptionHandler());
        inputThread.setName("DataManager-Input-" + threadNameFormat);
        inputThread.start();

        Thread outputThread = new Thread(() -> {
            while (running) {
                while (dataQueue.outgoingHasNextPacket()) {
                    Packet packet = dataQueue.outgoingNextPacket();
                    String json = packet.toString();
                    out.println(json);
                    System.out.println("Sent: " + packet.toString());
                }
            }
        });
        outputThread.setUncaughtExceptionHandler(new Utility.GlobalExceptionHandler());
        outputThread.setName("DataManager-Output-" + threadNameFormat);
        outputThread.start();
    }
}
