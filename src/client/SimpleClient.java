package client;
import java.io.*;
import java.net.*;
import java.security.*;
import java.util.Map;

import javax.crypto.*;

import conn.Packet;
import conn.Security;

public class SimpleClient {
    
    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        String serverAddress = "localhost"; // Use localhost to connect to the server running on the same machine
        int port = 58008; // Change this to the port number of your server

        try (Socket socket = new Socket(serverAddress, port);
                BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server on " + serverAddress + ":" + port);
            Packet logonPacket = new Packet("LOGON", Map.of("enc", "false", "username", "HW"));
            out.println(logonPacket.toString());

            // Read messages from the server and print them
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println("Server says: " + serverResponse);
                Packet packet = new Packet(serverResponse);
                if (packet.getType().equals("CHECKUP")) {
                    out.println(handleCheckupPacket(packet).toString());
                }        
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static Packet handleCheckupPacket(Packet packet) {
        // Send back a checkup packet, with the same headers, but content is the hash of the content
        String hash = packet.toString().hashCode() + "";
        return new Packet("CHECKUP", Map.of("enc", "false", "sent", packet.getInfo("sent"), "content", hash));
    }
}
