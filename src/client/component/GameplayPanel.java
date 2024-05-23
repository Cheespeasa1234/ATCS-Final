package client.component;

import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import conn.Packet.GameStatePacketData;

public class GameplayPanel extends JPanel {
    
    public GameStatePacketData gameStatePacketData;
    
    public GameplayPanel() {}

    public void setGameStatePacketData(GameStatePacketData gameStatePacketData) {
        this.gameStatePacketData = gameStatePacketData;
    }
    
    @Override public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.drawString("Hello, World!", 10, 10);
        g2.drawString("Time: " + (System.currentTimeMillis() - gameStatePacketData.nextStateChange), 10, 30);
    }
}
