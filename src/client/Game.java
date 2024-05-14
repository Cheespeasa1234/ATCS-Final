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

import client.component.ChatBox;
import conn.Packet;

public class Game extends JPanel {

    public static final int PREF_W = 800;
    public static final int PREF_H = 600;

    private SimpleClient client;
    private ChatBox chatbox;

    public Game() {
        this.setFocusable(true);
        this.setBackground(Color.WHITE);

        chatbox = new ChatBox();
        chatbox.sendChatButton.addActionListener(e -> {
            String message = chatbox.inputBox.getText();
            client.packetQueue.outgoingAddPacket(Packet.PacketFactory.chatMessagePacket("CLIENT", message));

            chatbox.addToChatbox("YOU: " + message);
        });
        this.add(chatbox);

        client = new SimpleClient("localhost", 58008);

        new Thread(new Runnable() {
            @Override public void run() {
                while (true) {
                    while (client.packetQueue.outgoingHasNextPacket()) {
                        Packet packet = client.packetQueue.outgoingNextPacket();
                        client.sendPacket(packet);
                        System.out.println("Game.java Sent: " + packet.toString());
                    }

                    while (client.packetQueue.incomingHasNextPacket()) {
                        Packet packet = client.packetQueue.incomingNextPacket();
                        System.out.println("Game.java Recieved: " + packet.toString());

                        if (packet.getType().equals("CHAT")) {
                            chatbox.addToChatbox(packet.getInfo("sender") + ": " + packet.getInfo("content"));                            
                        }
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
