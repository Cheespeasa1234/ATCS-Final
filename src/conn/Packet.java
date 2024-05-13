package conn;

import java.security.*;
import java.util.HashMap;
import java.util.Map;

public class Packet {

    /**
     * Client Packet types:
     * 
     */

    private HashMap<String, String> packetInfo;
    private String packetType;
    private String timeRecieved;

    public Packet(String bytes) {

        // parse the headers
        String[] parts = bytes.split("&");
        this.packetType = parts[0];       
        String[] headers = parts[1].split(",");
        this.packetInfo = new HashMap<String, String>();
        for (String rawHeader: headers) {
            String[] kv = rawHeader.split("=");
            this.packetInfo.put(kv[0].trim(), kv[1].trim());
        }

        

    }

    public Packet(String type, Map<String, String> info) {
        this.packetType = type;
        this.packetInfo = new HashMap<String, String>(info);
    }

    public String toString() {
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
