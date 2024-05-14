package client.component;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class ChatBox extends JPanel {

    public JTextField inputBox;
    private JList<String> chatMessageContainer;
    private DefaultListModel<String> chatMessageModel;
    public JButton sendChatButton;

    public ChatBox() {
        this.setLayout(new BorderLayout());

        // Set up the chats
        chatMessageModel = new DefaultListModel<>();
        chatMessageContainer = new JList<String>(chatMessageModel);
        chatMessageContainer.setFixedCellWidth(400);

        // Set up the inputs
        JPanel inputBoxContainer = new JPanel();
        inputBox = new JTextField() {
            @Override
            public java.awt.Dimension getPreferredSize() {
                return new java.awt.Dimension(200, super.getPreferredSize().height);
            }
        };
        sendChatButton = new JButton("Send");
        inputBoxContainer.add(inputBox);
        inputBoxContainer.add(sendChatButton);

        this.add(inputBoxContainer, BorderLayout.SOUTH);
        this.add(chatMessageContainer, BorderLayout.NORTH);
    }

    public void addToChatbox(String str) {
        SwingUtilities.invokeLater(() -> {
            chatMessageModel.addElement(str);
            chatMessageContainer.ensureIndexIsVisible(chatMessageModel.size() - 1);
            this.revalidate();
            this.repaint();
        });
    }
}
