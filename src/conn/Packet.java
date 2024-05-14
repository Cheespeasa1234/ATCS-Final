package conn;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class Packet {

    /**
     * Client Packet types:
     * LOGON: Headers have username and enc status. Content is discarded.
     * CHAT: sender:String,content:String
     */

    private HashMap<String, String> packetInfo;
    private String packetType;

    public Packet(String bytes) {

        // parse the headers
        String[] parts = bytes.split("&");
        this.packetType = parts[0];
        String[] headers = parts[1].split("#");
        this.packetInfo = new HashMap<String, String>();
        for (String rawHeader : headers) {
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
            data += entry.getKey() + "=" + entry.getValue() + "#";
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

    public Packet enforcePacketHasProperty(String property) {
        if (!this.packetInfo.containsKey(property) || this.packetInfo.get("property") == "" )
            throw new TypeNotPresentException(property, new Exception(property + " does not exist in packet."));
        return this;
    }

    public static class PacketTypeEnforcer {

        public static HashMap<String, String[]> packetTypeRules = new HashMap<String, String[]>() {{
            put("CHAT",  new String[] {"sender", "content"});
        }};

    }

    public static class PacketFactory {

        public static String sanitize(String text) {
            // Remove all characters except allowed ones
            String sanitizedText = text.replaceAll("[^a-zA-Z0-9!.,?/\\s]", "");

            // Replace newlines and tabs with spaces
            sanitizedText = sanitizedText.replaceAll("[\n\t]", " ");

            return sanitizedText;
        }

        public static Packet createStandardPacket(String packetType, String[] keys, String[] values) {

        }

    }
}
