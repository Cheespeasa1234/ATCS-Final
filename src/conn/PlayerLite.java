package conn;

public class PlayerLite {

    public static enum PlayerStatus {
        ACTIVE, WAITING, TYPING, WORKING
    }
    public PlayerStatus status;

    public transient String ip;
    public String displayName;

    public Packet.VotePacketData vote;
    
    public PlayerLite(String ip) {
        this.ip = ip;
        this.status = PlayerStatus.WAITING;
    }
}
