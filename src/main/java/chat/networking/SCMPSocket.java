package chat.networking;

import chat.SecureOp;
import chat.config.ChannelConfig;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SCMPSocket extends MulticastSocket {
    //vId || sID || SMCPmsgType || SAttributes || SizeOfSecurePayload || SecurePayload || FastSecureMCheck

    private static byte[] ivBytes = new byte[]{
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
    };
    private static IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
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
            send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void receive(DatagramPacket packet) throws IOException {
        super.receive(packet);
        try {
            byte[] plainText = decode(packet.getData());
            packet.setData(plainText);
        } catch (TamperedException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private byte[] encode(byte[] plainText) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        dataStream.writeByte(1); // TODO Protocol version
        dataStream.writeUTF("127:0:0:1:1234"); // TODO Chat session
        dataStream.writeByte(1); // TODO MessageType

        // SAttributes
        dataStream.writeUTF(channelConfig.getChatID());
        dataStream.writeUTF("session name"); // TODO
        dataStream.writeUTF(channelConfig.getSymmetricAlgorithm());
        dataStream.writeUTF(channelConfig.getMode());
        dataStream.writeUTF(channelConfig.getPaddingAlgorithm());
        dataStream.writeUTF(channelConfig.getIntegrityHash());
        dataStream.writeUTF(channelConfig.getMacAlgorithm());

        //Payload
        byte[] cipherText = SecureOp.encrypt(channelConfig.getCipher(), plainText, channelConfig.getSymmetricKey(), ivSpec);
        dataStream.writeInt(cipherText.length);
        dataStream.write(cipherText);

        // Fast hash
        byte[] message = byteStream.toByteArray();
        byte[] digest = SecureOp.calculateHMAC(channelConfig.getMac(), channelConfig.getMacKey(), message);
        dataStream.write(digest);

        return byteStream.toByteArray();
    }

    private byte[] decode(byte[] message) throws TamperedException, InvalidKeyException, IOException, NoSuchAlgorithmException, NoSuchPaddingException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(message);
        DataInputStream dataStream = new DataInputStream(byteStream);

        dataStream.readByte();
        String chatSession = dataStream.readUTF();
        byte messageType = dataStream.readByte();

        if (messageType != 0x01) {
            // Ó palhaço
        }

        // SAttributes
        String chatID = dataStream.readUTF();
        String symmetricAlgorithmName = dataStream.readUTF();
        String sessionName = dataStream.readUTF();
        String mode = dataStream.readUTF();
        String paddingAlgorithm = dataStream.readUTF();
        String integrityHash = dataStream.readUTF();
        String macAlgorithm = dataStream.readUTF();
        Cipher cipher = Cipher.getInstance(symmetricAlgorithmName + "/" + mode + "/" + paddingAlgorithm);
        Mac mac = Mac.getInstance(macAlgorithm);
        Mac fastMac = Mac.getInstance(integrityHash);

        //Payload
        int cipherTextLength = dataStream.readInt();
        byte[] cipherText = dataStream.readNBytes(cipherTextLength);

        // Fast hash
        byte[] digest = dataStream.readNBytes(fastMac.getMacLength());
        if (dataStream.available() > 0) {
            // Ó palhaço
        }

        if (!SecureOp.isValidHMAC(mac, message, channelConfig.getMacKey(), digest)) {
            throw new TamperedException();
        }

        return SecureOp.decrypt(cipher, cipherText, channelConfig.getSymmetricKey(), ivSpec);
    }
}
