package controller;

import chat.Message;
import chat.config.ChannelConfig;
import chat.networking.SCMPSocket;
import javafx.application.Platform;
import view.ChatPane;
import view.SpeechAuthor;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.security.NoSuchAlgorithmException;

public class ChatController {
    SCMPSocket socket;
    private ChatPane chatPane;
    private AppController appController;
    ChannelConfig channelConfig;
    private String username;
    private int messageCount = 0; // Used to generate sequence number
    private boolean stopFlag = false;
    private Thread inputHandler;

    public static final int DEFAULT_SOCKET_TIMEOUT_MILLIS = 5000;

    public ChatController(AppController appController, ChatPane chatPane, ChannelConfig channelConfig, String username) {
        this.chatPane = chatPane;
        this.username = username;
        this.channelConfig = channelConfig;
        this.appController = appController;
        try {
            socket = new SCMPSocket(channelConfig);
            socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT_MILLIS);
            socket.setTimeToLive(1);
            socket.joinGroup(channelConfig.getAddress().getAddress());
            inputHandler = new InputHandler();
            inputHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void deliver(Message message) {
        Platform.runLater(() -> chatPane.addMessage(message.getAuthor(), message.getText(), SpeechAuthor.OTHER));

    }

    public void shutdown() throws InterruptedException {
        stopFlag = true;
        inputHandler.join();
    }

    public void sendMessage(String messageText) {
        Message message = Message.text(username, messageText);
        byte[] buffer = new byte[65508];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, channelConfig.getAddress().getAddress(), channelConfig.getAddress().getPort());
        packet.setData(message.serialize(channelConfig));
        try {
            socket.send(packet);
            chatPane.addMessage(username, messageText, SpeechAuthor.SELF);
        } catch (IOException e) {
            chatPane.addMessage(username, "Message failed to send", SpeechAuthor.SELF);
        }
    }

    class InputHandler extends Thread {
        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[65508];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (!stopFlag) {
                try {
                    packet.setLength(buffer.length);
                    socket.receive(packet);
                    deliver(Message.deserialize(packet.getData(), channelConfig));

                } catch (InterruptedIOException e) {
                    // Timeout used to periodically check stop flag
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
            socket.close();
        }
    }
}
