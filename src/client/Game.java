package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import client.component.ChatBox;

public class Game extends JPanel {

    public static final int PREF_W = 800;
    public static final int PREF_H = 600;

    private SimpleClient client;
    private ChatBox chatbox;
    private Gson gson = new Gson();

    public Game() {
        this.setFocusable(true);
        this.setBackground(Color.WHITE);

        chatbox = new ChatBox();
        chatbox.sendChatButton.addActionListener(e -> {
            String message = chatbox.inputBox.getText();
            client.dataManager.dataQueue.outgoingAddPacket(gson.fromJson(
                "{ \"type\": \"CHAT\", \"data\": { \"content\": \"" + message + "\" } }", JsonObject.class));

            chatbox.addToChatbox("YOU: " + message);
        });
        this.add(chatbox);

        client = new SimpleClient("localhost", 58008);

        new Thread(() -> {
            while (true) {
                while (client.dataManager.dataQueue.incomingHasNextPacket()) {
                    JsonObject packet = client.dataManager.dataQueue.incomingNextPacket();
                    System.out.println("Game.java Recieved: " + packet.toString());

                    String type = packet.get("type").getAsString();
                    if (type.equals("CHAT")) {
                        String message = packet.get("data").getAsJsonObject().get("content").getAsString();
                        String sender = packet.get("data").getAsJsonObject().get("sender").getAsString();
                        chatbox.addToChatbox(sender + ": " + message);
                    }
                }
            }
        }).start();

        new Thread(client).start();
    }

    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

    }

    /* METHODS FOR CREATING JFRAME AND JPANEL */

    public Dimension getPreferredSize() {
        return new Dimension(PREF_W, PREF_H);
    }

    public static void createAndShowGUI() {
        JFrame frame = new JFrame("You're Mother");
        JPanel gamePanel = new Game();

        frame.getContentPane().add(gamePanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

}
