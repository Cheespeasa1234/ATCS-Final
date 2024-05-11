package conn;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Packet {

    /**
     * Client Packet types:
     * LOGON: Headers have username and enc status. Content is discarded.
     * CHAT: Headers have enc status. Content is the message. Message is to be displayed in the chat room.
     */

    private HashMap<String, String> packetInfo;
    private String rawPacketContent;
    private String packetType;
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
        this.rawPacketContent = bytes.substring(parts[0].length() + parts[1].length() + 2);

    }

    public Packet(String type, Map<String, String> info, String content) {
        this.packetType = type;
        this.packetInfo = new HashMap<String, String>(info);
        this.rawPacketContent = content;
    }

    public String toString() {
        String headers = "";
        for (Map.Entry<String, String> entry : this.packetInfo.entrySet()) {
            headers += entry.getKey() + "=" + entry.getValue() + ",";
        }
        headers = headers.substring(0, headers.length() - 1);

        String content = this.rawPacketContent;
        if (content.equals("")) {
            content = "<empty>";
        }
        return this.packetType + "&" + headers + "&" + content;
    }

    public String getInfo(String key) {
        return this.packetInfo.get(key);
    }

    public String getContent() {
        return this.rawPacketContent;
    }

    public String getType() {
        return this.packetType;
    }
}
