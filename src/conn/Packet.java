package conn;

import java.util.HashMap;
import java.util.Map;

public class Packet {

    private HashMap<String, String> packetInfo;
    private String packetType;
    private long timeRecieved;
    public String latency;

    /**
     * Read and assemble a packet read in from the network.
     * This constructor also calculates the latency of the packet, and other metadata.
     */
    public Packet(String bytes) {

        // record the time the packet was recieved
        this.timeRecieved = System.currentTimeMillis();
    
        // parse the headers
        String[] parts = bytes.split("&");
        this.packetType = parts[0];       
        String[] headers = parts[1].split(",");
        this.packetInfo = new HashMap<String, String>();
        for (String rawHeader: headers) {
            String[] kv = rawHeader.split("=");
            this.packetInfo.put(kv[0].trim(), kv[1].trim());
        }

        // calculate the latency
        String timeSent = this.packetInfo.get("sent");
        this.latency = Long.toString(this.timeRecieved - Long.parseLong(timeSent));

    }

    public Packet(String type, Map<String, String> info) {
        this.packetType = type;
        this.packetInfo = new HashMap<String, String>(info);
    }

    /**
     * Prepare the packet for sending over the network.
     * This should be the last function ever called on a packet before sending it.
     * @return The packet in string notation.
     */
    @Override public String toString() {

        // Add metadata
        this.packetInfo.put("sent", Long.toString(System.currentTimeMillis()));

        String data = "";
        for (Map.Entry<String, String> entry : this.packetInfo.entrySet()) {
            data += entry.getKey() + "=" + entry.getValue() + ",";
        }
        data = data.substring(0, data.length() - 1);
        return this.packetType + "&" + data;
    }

    public String getInfo(String key) {
        return this.packetInfo.get(key);
    }

    public String getType() {
        return this.packetType;
    }
}
