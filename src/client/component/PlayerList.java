package client.component;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

import conn.PlayerLite;

/**
 * A Swing component that displays all players as a list and formats them nicely.
 */
public class PlayerList extends JPanel {
    
    private static class PlayerDisplay extends JPanel {
        public PlayerDisplay(PlayerLite player) {
            JLabel nameLabel = new JLabel(player.displayName);
            JLabel statusLabel = new JLabel(player.status.toString());
            statusLabel.setForeground(Color.GRAY);

            this.add(nameLabel);
            this.add(statusLabel);
        }
    }
    
    public JPanel playersPanel;
    public PlayerList() {
        this.playersPanel = new JPanel();
        this.add(playersPanel);
    }

    public void updatePlayers(PlayerLite[] players) {
        if (players == null) {
            return;
        }
        
        playersPanel.removeAll();
        for (PlayerLite player : players) {
            playersPanel.add(new PlayerDisplay(player));
        }
        this.revalidate();
        this.repaint();
    }
}
