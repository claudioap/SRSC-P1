package chat.networking;

import chat.SecureOp;
import chat.config.ChannelConfig;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class SCMPSocket extends MulticastSocket {

    private static byte[] ivBytes = new byte[]{
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    private IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
    private ChannelConfig channelConfig;

    public SCMPSocket(ChannelConfig channelConfig) throws IOException {
        super(channelConfig.getAddress().getPort());
        this.channelConfig = channelConfig;
    }


    @Override
    public void send(DatagramPacket packet) throws IOException {
        byte[] payload;
        try {
            payload = encode(packet.getData());
            packet.setData(payload);
            packet.setLength(payload.length);

            System.out.println("Payload set to " + payload.length);
            System.out.println("Payload2 set to " + packet.getLength());
            super.send(packet);

        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    @Override
    public void receive(DatagramPacket packet) throws IOException {
        super.receive(packet);
        System.out.println("Payload received " + packet.getLength());

        byte[] message = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        try {
            byte[] plainText = decode(message);
            packet.setData(plainText);
        } catch (TamperedException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private byte[] encode(byte[] plainText) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        dataStream.writeByte(1); // TODO Protocol version
//        dataStream.writeUTF(channelConfig.getHashedIdentifier()); // TODO Chat session
//        dataStream.writeByte(1); // TODO MessageType

        // SAttributes
        dataStream.writeUTF(channelConfig.getChatID());
//        dataStream.writeUTF("session name"); // TODO
        dataStream.writeUTF(channelConfig.getSymmetricAlgorithm());
        dataStream.writeUTF(channelConfig.getMode());
        dataStream.writeUTF(channelConfig.getPaddingAlgorithm());
//        dataStream.writeUTF(channelConfig.getIntegrityHash());
        dataStream.writeUTF(channelConfig.getMacAlgorithm());

        //Payload
        byte[] cipherText = SecureOp.encrypt(channelConfig.getCipher(), ByteBuffer.wrap(plainText), channelConfig.getSymmetricKey(), null);
        System.out.println("S PT " + plainText.length);
        System.out.println("S CT " + cipherText.length);
        dataStream.writeInt(cipherText.length);
        dataStream.write(cipherText);

        // Fast hash
        byte[] message = byteStream.toByteArray();
        byte[] authenticityDigest = SecureOp.calculateHMAC(channelConfig.getMac(), channelConfig.getMacKey(), message);
        dataStream.write(authenticityDigest);
        byte[] payload = byteStream.toByteArray();
        System.out.println("S PL " + payload.length);

        return payload;
    }

    private byte[] decode(byte[] message) throws TamperedException, InvalidKeyException, IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(message);
        DataInputStream dataStream = new DataInputStream(byteStream);

        System.out.println("R PL " + message.length);

        byte protocolVersion = dataStream.readByte();
//        String chatSession = dataStream.readUTF();
//        Message.MessageType messageType = Message.MessageType.fromCode(dataStream.readByte());

//        if (messageType != 0x01) {
//            // Ó palhaço
//        }

        // SAttributes
        String chatID = dataStream.readUTF();
        if (!chatID.equals(channelConfig.getChatID())) {
            // Ó palhaço
        }
        String symmetricAlgorithmName = dataStream.readUTF();
        String mode = dataStream.readUTF();
        String paddingAlgorithm = dataStream.readUTF();
//        String integrityHash = dataStream.readUTF();
        String macAlgorithm = dataStream.readUTF();
        Cipher cipher = Cipher.getInstance(symmetricAlgorithmName + "/" + mode + "/" + paddingAlgorithm);
        Mac authenticityDigest = Mac.getInstance(macAlgorithm);
//        MessageDigest integrityDigest  = MessageDigest.getInstance(integrityHash);

        //Payload
        int cipherTextLength = dataStream.readInt();
        byte[] cipherText = dataStream.readNBytes(cipherTextLength);

        // Fast hash
        byte[] hmac = dataStream.readNBytes(authenticityDigest.getMacLength());
        if (dataStream.available() > 0) {
            // Ó palhaço
        }
        byte[] authenticatedSegment = Arrays.copyOfRange(message, 0, message.length - authenticityDigest.getMacLength());
        SecureOp.assertValidHMAC(authenticityDigest, authenticatedSegment, channelConfig.getMacKey(), hmac);
        byte[] plainText = SecureOp.decrypt(cipher, ByteBuffer.wrap(cipherText), channelConfig.getSymmetricKey(), null);
        System.out.println("R CT " + cipherText.length);
        System.out.println("R PT " + plainText.length);
        return plainText;
    }
}
