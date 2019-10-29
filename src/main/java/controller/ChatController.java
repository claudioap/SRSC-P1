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
import java.util.HashSet;
import java.util.Set;

public class ChatController {
    SCMPSocket socket;
    private ChatPane chatPane;
    private AppController appController;
    ChannelConfig channelConfig;
    private String username;
    private int messageCount = 0; // Used to generate sequence number
    private boolean stopFlag = false;
    private Thread inputHandler;
    private Set<String> knownPeers = new HashSet<>();

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
            Message joinMessage = Message.join(username);
            sendMessage(joinMessage);
            inputHandler = new InputHandler();
            inputHandler.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deliver(Message message) {
        switch (message.getType()) {
            case JOIN:
                System.out.println("Join arrived");
                Platform.runLater(() -> {
                    chatPane.addMessage(message.getAuthor(), message.getAuthor() + " has joined.", null);
                    chatPane.addUser(message.getAuthor());
                });
                break;
            case LEAVE:
                System.out.println("Leave arrived");
                Platform.runLater(() -> {
                    chatPane.addMessage(message.getAuthor(), message.getAuthor() + " has left.", null);
                    chatPane.removeUser(message.getAuthor());
                });
                break;
            case TEXT:
                System.out.println("Text arrived");
                Platform.runLater(() -> {
                    if (message.getAuthor().equals(username)) {
                        Platform.runLater(() -> chatPane.addMessage(message.getAuthor(), message.getText(), SpeechAuthor.SELF));
                    } else {
                        Platform.runLater(() -> chatPane.addMessage(message.getAuthor(), message.getText(), SpeechAuthor.OTHER));
                    }
                    chatPane.addUser(message.getAuthor());
                });
                break;
        }
    }

    public void sendMessage(String messageText) {
        Message message = Message.text(username, messageText);
        byte[] buffer = new byte[65508];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, channelConfig.getAddress().getAddress(), channelConfig.getAddress().getPort());
        packet.setData(message.serialize());
        try {
            socket.send(packet);
        } catch (IOException e) {
            chatPane.addMessage(username, "Message failed to send", SpeechAuthor.SELF);
        }
    }

    private void sendMessage(Message message) throws IOException {
        byte[] payload = message.serialize();
        byte[] buffer = new byte[65508];
        DatagramPacket packet = new DatagramPacket(
                buffer,
                buffer.length,
                channelConfig.getAddress().getAddress(),
                channelConfig.getAddress().getPort());

        packet.setData(payload);
        packet.setLength(payload.length);
        socket.send(packet);
    }

    public void shutdown() throws InterruptedException, IOException {
        stopFlag = true;
        Message leaveMessage = Message.leave(username);
        sendMessage(leaveMessage);
        inputHandler.join();
    }

    class InputHandler extends Thread {
        @Override
        public void run() {
            super.run();
            byte[] buffer = new byte[65508];
            while (!stopFlag) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    deliver(Message.deserialize(packet.getData()));
                } catch (InterruptedIOException e) {
                    // Timeout used to periodically check stop flag
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            socket.close();
        }
    }
}
