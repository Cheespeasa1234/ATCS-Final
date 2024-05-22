package conn;

public class PlayerLite {
    public static enum PlayerStatus {
        ACTIVE, WAITING, TYPING, WORKING
    }
    public transient String ip;
    public String displayName;
    public PlayerStatus status;
    public PlayerLite(String ip) {
        this.ip = ip;
        this.status = PlayerStatus.WAITING;
    }
}
