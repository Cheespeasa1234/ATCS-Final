package conn;

import java.util.LinkedList;
import java.util.Queue;

public class PacketQueue {
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
