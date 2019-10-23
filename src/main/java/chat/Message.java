package chat;

import chat.config.ChannelConfig;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Message {
    private String author;
    private MessageType type;
    private String text;

    public static Message join(String author) {
        Message message = new Message();
        message.type = MessageType.JOIN;
        message.author = author;
        return message;
    }

    public static Message leave(String author) {
        Message message = new Message();
        message.type = MessageType.LEAVE;
        message.author = author;
        return message;
    }

    public static Message text(String author, String text) {
        Message message = new Message();
        message.type = MessageType.TEXT;
        message.author = author;
        message.text = text;
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public MessageType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public byte[] serialize(ChannelConfig config) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        try {
            dataStream.writeUTF(author);
            dataStream.writeInt(0); // TODO nonce
            dataStream.writeInt(type.code);
            if (type == MessageType.TEXT) {
                dataStream.writeUTF(text);
            }
            byte[] message = byteStream.toByteArray();
            MessageDigest digest = config.getDigest();
            digest.reset();
            digest.update(message);
            byte[] integrityCheck = digest.digest();
            dataStream.writeUTF(config.getIntegrityHash());
            dataStream.write(integrityCheck);
            dataStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return byteStream.toByteArray();
    }

    public static Message deserialize(byte[] data, ChannelConfig channelConfig) throws IOException, NoSuchAlgorithmException {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data, 0, data.length));
        String author = inputStream.readUTF();
        int nounce = inputStream.readInt();
        MessageType type = MessageType.fromCode(inputStream.readInt());

        String text = null;
        switch (type) {
            case JOIN:
            case LEAVE:
                break;
            default:
                text = inputStream.readUTF();
        }


        String digestName = inputStream.readUTF();
        MessageDigest digest = MessageDigest.getInstance(digestName);
        byte[] hash = inputStream.readNBytes(digest.getDigestLength());
        if (inputStream.available() > 0) {
            // Ó palhaço
        }
        byte[] hashedData = Arrays.copyOfRange(data, 0, data.length - hash.length);
        if (!SecureOp.isValidHash(digest, hashedData, hash)) {
            // Ó palhaço
        }

        switch (type) {
            case JOIN:
                return join(author);
            case LEAVE:
                return leave(author);
            case TEXT:
                return text(author, text);
        }
        throw new RuntimeException("Ó palhaço");
    }

    public enum MessageType {
        JOIN(0), LEAVE(1), TEXT(2);

        int code;

        MessageType(int code) {
            this.code = code;
        }

        static MessageType fromCode(int code) {
            switch (code) {
                case 0:
                    return JOIN;
                case 1:
                    return LEAVE;
                case 2:
                    return TEXT;
            }
            throw new IllegalArgumentException();
        }
    }
}
