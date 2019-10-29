package chat;

import java.io.*;

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

    public byte[] serialize() {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        try {
            dataStream.writeUTF(author);
            dataStream.writeInt(type.code);
            if (type == MessageType.TEXT) {
                dataStream.writeUTF(text);
            }
            dataStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        return byteStream.toByteArray();
    }

    public static Message deserialize(byte[] data) throws IOException {
        DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(data, 0, data.length));
        String author = inputStream.readUTF();
        MessageType type = MessageType.fromCode(inputStream.readInt());

        switch (type) {
            case JOIN:
                return join(author);
            case LEAVE:
                return leave(author);
            case TEXT:
                String text = inputStream.readUTF();
                return text(author, text);
        }
        throw new RuntimeException("Unable to deserialize");
    }

    public enum MessageType {
        JOIN(0), LEAVE(1), TEXT(2);

        int code;

        MessageType(int code) {
            this.code = code;
        }

        public static MessageType fromCode(int code) {
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
