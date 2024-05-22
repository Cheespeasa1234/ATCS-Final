package conn;

import server.ClientHandler;

public class PlayerLiteConn extends PlayerLite {
    public transient ClientHandler clientConnection;
    public PlayerLiteConn(String ip) {
        super(ip);
    }
}
