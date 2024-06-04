package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import client.component.ChatBox;
import client.component.PlayerList;
import conn.Packet;
import conn.PlayerLite;
import conn.Utility;
import conn.Packet.SubmitPromptPacketData;

public class Game extends JPanel {

    public static final int PREF_W = 800;
    public static final int PREF_H = 500;

    private SimpleClient client;
    private ChatBox chatbox;
    private PlayerList playerList;
    private JTextField usernameField, ipField, portField, promptField;
    private JButton submitContentButton;

    private JPanel logonMenuPanel;
    private JPanel gamePanel;
    private JPanel gameplayPaintingPanel;

    private PlayerLite[] players;
    private PlayerLite currentPlayer;
    private Utility.GameState gameState;
    private long nextGameStateChange;
    private int stateChangeDelay;

    public void connect(String hostname, int port, String username) {
        // When the client starts, add a termination event to the datamanager
        client = new SimpleClient(hostname, port, username, () -> {
            logonMenuPanel.setVisible(false);
            gamePanel.setVisible(true);
            client.dataManager.addInputTerminationEvent(() -> {
                System.out.println("Connection terminated.");
                System.exit(0);
            });
        });

        Thread inputThread = new Thread(() -> {
            System.out.println("game input loop started");
            while (true) {
                while (client.dataManager != null && client.dataManager.dataQueue.incomingHasNextPacket()) {
                    Packet packet = client.dataManager.dataQueue.incomingNextPacket();

                    if (packet.type.equals(Packet.ChatPacketData.typeID)) {
                        String message = packet.chatPacketData.message;
                        String sender = packet.chatPacketData.sender;
                        chatbox.addToChatbox(sender + ": " + message);
                    } else if (packet.type.equals(Packet.GameStatePacketData.typeID)) {
                        players = packet.gameStatePacketData.players;
                        gameState = packet.gameStatePacketData.gameState;
                        nextGameStateChange = packet.gameStatePacketData.nextStateChange;
                        stateChangeDelay = packet.gameStatePacketData.stateChangeDelay;
                        playerList.updatePlayers(players);
                    } else if (packet.type.equals(Packet.ElectionPacketData.typeID)) {
                        // Get the current topic
                        String question = packet.startVotePacketData.election.question;
                        List<SubmitPromptPacketData> candidates = packet.startVotePacketData.election.candidates;
                    } else if (packet.type.equals(Packet.ServerMessagePacketData.typeID)) {
                        System.out.println("Server message packet recieved.");
                        System.out.println("Server message packet recieved. Adding to screen now.");
                        String message = packet.serverMessagePacketData.message;
                        chatbox.addToChatbox("Server: " + message);
                    } else if (packet.type.equals(Packet.PlayerPacketData.typeID)) {
                        PlayerLite player = packet.playerData.player;
                        currentPlayer = player;
                    } else {
                        System.out.println("Unknown packet type: " + packet.type);
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, Utility.getStackTraceString(e), e.toString(), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        inputThread.setUncaughtExceptionHandler(new Utility.GlobalExceptionHandler());
        inputThread.setName("Input");
        inputThread.start();

        Thread clientThread = new Thread(client);
        clientThread.setUncaughtExceptionHandler(new Utility.GlobalExceptionHandler());
        clientThread.setName("Client");
        clientThread.start();
    }

    public Game() {
        this.setFocusable(true);
        this.setBackground(Color.WHITE);

        makeLogonScreenPanel();
        makeGamePanel();

        logonMenuPanel.setVisible(true);
        gamePanel.setVisible(false);

        this.add(logonMenuPanel);
        this.add(gamePanel);

        repaintTimer.start();
    }

    private Timer repaintTimer = new Timer(1000 / 60, e -> {
        repaint();
        gameplayPaintingPanel.repaint();
    });

    // Initialization of gamePanel and infoPanel
    private void makeGamePanel() {
        int infoPanelWidth = 400;

        gamePanel = new JPanel(new BorderLayout());
        gamePanel.setPreferredSize(new Dimension(PREF_W, PREF_H));

        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setPreferredSize(new Dimension(infoPanelWidth, PREF_H));

        // Initialization of playerList
        initPlayerList(infoPanelWidth);

        // Initialization of chatbox
        initChatbox();

        // Initialization of gameplayPaintingPanel
        initGameplayPaintingPanel(infoPanelWidth);

        // Add chatbox and playerList to infoPanel
        chatbox.setPreferredSize(new Dimension(infoPanelWidth, 300));
        infoPanel.add(chatbox, BorderLayout.SOUTH);
        infoPanel.add(playerList, BorderLayout.NORTH);

        // Add infoPanel and gameplayPaintingPanel to gamePanel
        gamePanel.add(infoPanel, BorderLayout.EAST);
        gamePanel.add(gameplayPaintingPanel, BorderLayout.WEST);
    }

    // Initialization of playerList
    private void initPlayerList(int infoPanelWidth) {
        playerList = new PlayerList();
        playerList.setPreferredSize(new Dimension(infoPanelWidth, PREF_H - 300));
        playerList.updatePlayers(players);
    }

    // Initialization of chatbox
    private void initChatbox() {
        chatbox = new ChatBox();
        chatbox.inputBox.addFocusListener(new FocusListener() {
            @Override public void focusGained(FocusEvent e) {
                System.out.println("Focus gained");
                client.dataManager.dataQueue.outgoingAddPacket(Packet.statusPacket(PlayerLite.PlayerStatus.TYPING));
            }

            @Override public void focusLost(FocusEvent e) {
                System.out.println("Focus lost");
                client.dataManager.dataQueue.outgoingAddPacket(Packet.statusPacket(PlayerLite.PlayerStatus.ACTIVE));
            }
        });

        chatbox.sendChatButton.addActionListener(e -> {
            String message = chatbox.inputBox.getText();
            chatbox.inputBox.setText("");
            Packet packet = Packet.chatPacket(message, usernameField.getText());
            client.dataManager.dataQueue.outgoingAddPacket(packet);
            System.out.println("added chat packet to queue");
        });
    }

    // Initialization of gameplayPaintingPanel
    private void initGameplayPaintingPanel(int infoPanelWidth) {
        gameplayPaintingPanel = new JPanel() {
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.drawString("Game State: " + gameState, 10, 10);
                drawTimer(g2);
            }

            public void drawTimer(Graphics2D g2) {
                double progress = (double) (nextGameStateChange - System.currentTimeMillis()) / stateChangeDelay;
                g2.fillArc(100, 100, 100, 100, 0, (int) (progress * 360));
            }
        };

        JTextField promptField = new JTextField();
        promptField.setText("Prompt");
        gameplayPaintingPanel.add(promptField);

        JButton submitContentButton = new JButton("Submit");
        submitContentButton.addActionListener(e -> {
            String prompt = promptField.getText();
            client.dataManager.dataQueue.outgoingAddPacket(Packet.submitPromptPacket(prompt, currentPlayer));
        });
        gameplayPaintingPanel.add(submitContentButton);

        gameplayPaintingPanel.setPreferredSize(new Dimension(PREF_W - infoPanelWidth, PREF_H));
    }

    private void makeLogonScreenPanel() {
        logonMenuPanel = new JPanel(new BorderLayout());
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

        logonMenuPanel.add(usernameField, BorderLayout.NORTH);
        logonMenuPanel.add(connectPanel, BorderLayout.CENTER);
        logonMenuPanel.add(connectButton, BorderLayout.SOUTH);
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
