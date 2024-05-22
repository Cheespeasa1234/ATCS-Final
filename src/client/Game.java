package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import client.component.ChatBox;
import client.component.PlayerList;
import conn.DataManager;
import conn.Packet;
import conn.PlayerLite;

public class Game extends JPanel {

    public static final int PREF_W = 800;
    public static final int PREF_H = 600;

    private SimpleClient client;
    private ChatBox chatbox;
    private PlayerList playerList;
    private JTextField usernameField, ipField, portField;

    private JPanel mainMenuPanel;
    private JPanel gameplayPanel;

    private PlayerLite[] players;

    public void connect(String hostname, int port, String username) {
        // When the client starts, add a termination event to the datamanager
        client = new SimpleClient(hostname, port, username, () -> {
            mainMenuPanel.setVisible(false);
            gameplayPanel.setVisible(true);
            client.dataManager.addInputTerminationEvent(() -> {
                System.out.println("Connection terminated.");
                System.exit(0);
            });
        });

        new Thread(() -> {

            System.out.println("game input loop started");
            while (true) {
                while (client.dataManager != null && client.dataManager.dataQueue.incomingHasNextPacket()) {
                    Packet packet = client.dataManager.dataQueue.incomingNextPacket();

                    if (packet.type.equals("CHAT")) {
                        System.out.println("CHAT packet recieved.");
                        System.out.println("Chat packet recieved. Adding to screen now.");
                        String message = packet.chatPacketData.message;
                        String sender = packet.chatPacketData.sender;
                        chatbox.addToChatbox(sender + ": " + message);
                    } else if (packet.type.equals("GAMESTATE")) {
                        players = packet.gameStatePacketData.players;
                        playerList.updatePlayers(players);
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

        new Thread(client).start();

    }

    public Game() {
        this.setFocusable(true);
        this.setBackground(Color.WHITE);

        makeMainMenuPanel();
        makeGameplayPanel();

        mainMenuPanel.setVisible(true);
        gameplayPanel.setVisible(false);

        this.add(mainMenuPanel);
        this.add(gameplayPanel);
    }

    private void makeGameplayPanel() {
        gameplayPanel = new JPanel();
        chatbox = new ChatBox();
        chatbox.inputBox.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                System.out.println("Focus gained");
                client.dataManager.dataQueue.outgoingAddPacket(Packet.statusPacket(PlayerLite.PlayerStatus.TYPING));
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                System.out.println("Focus lost");
                client.dataManager.dataQueue.outgoingAddPacket(Packet.statusPacket(PlayerLite.PlayerStatus.ACTIVE));
            }
        });
        chatbox.sendChatButton.addActionListener(e -> {
            String message = chatbox.inputBox.getText();
            Packet packet = Packet.chatPacket(message, usernameField.getText());
            client.dataManager.dataQueue.outgoingAddPacket(packet);
            System.out.println("added chat packet to queue");
        });

        chatbox.setPreferredSize(new Dimension(400, 300));

        playerList = new PlayerList();
        playerList.setPreferredSize(new Dimension(400, PREF_H - 300));
        playerList.updatePlayers(players);

        gameplayPanel.add(chatbox);
        gameplayPanel.add(playerList);

    }

    private void makeMainMenuPanel() {
        mainMenuPanel = new JPanel(new BorderLayout());
        usernameField = new JTextField();
        usernameField.setText("Username");

        ipField = new JTextField();
        ipField.setText("Hostname");

        portField = new JTextField();
        portField.setText("58008");

        usernameField.setPreferredSize(new Dimension(200, 30));
        ipField.setPreferredSize(new Dimension(120, 30));
        portField.setPreferredSize(new Dimension(80, 30));

        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> {
            connect(ipField.getText(), Integer.parseInt(portField.getText()), usernameField.getText());
        });

        JPanel connectPanel = new JPanel();
        connectPanel.add(ipField);
        connectPanel.add(portField);

        mainMenuPanel.add(usernameField, BorderLayout.NORTH);
        mainMenuPanel.add(connectPanel, BorderLayout.CENTER);
        mainMenuPanel.add(connectButton, BorderLayout.SOUTH);
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
