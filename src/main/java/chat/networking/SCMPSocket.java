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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;

public class SCMPSocket extends MulticastSocket {

    public final static int PROTO_VERSION = 1;

    private static byte[] ivBytes = new byte[]{
            0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
            0x13, 0x37, 0x13, 0x37, 0x13, 0x37, 0x13, 0x37
    };
    private IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
    private ChannelConfig channelConfig;
    private int sequenceNumber = 0;
    private Random rng = new Random();

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
            super.send(packet);

        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    @Override
    public void receive(DatagramPacket packet) throws IOException {
        super.receive(packet);
        byte[] message = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
        try {
            byte[] plainText = decode(message);
            packet.setData(plainText);
        } catch (TamperedException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    private byte[] encode(byte[] plainText) throws IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, IOException {
        // TODO nounce
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        DataOutputStream dataStream = new DataOutputStream(byteStream);
        dataStream.writeByte(PROTO_VERSION);
        dataStream.writeUTF(channelConfig.getAddress().toString());
        dataStream.writeByte(1); // MessageType, TODO remove, perhaps

        // SAttributes
        dataStream.write(channelConfig.getChatIDDigest());
        dataStream.write(channelConfig.getSymAlgorithmDigest());
        dataStream.write(channelConfig.getModeDigest());
        dataStream.write(channelConfig.getPaddingDigest());
        dataStream.write(channelConfig.getIntegrityAlgorithmDigest());
        dataStream.write(channelConfig.getMacDigest());

        //Payload
        dataStream.writeInt(sequenceNumber++);
        dataStream.writeInt(rng.nextInt(Integer.MAX_VALUE));
        ByteArrayOutputStream plainTextWithIntegrity = new ByteArrayOutputStream();
        plainTextWithIntegrity.write(plainText);
        MessageDigest digest = channelConfig.getDigest();
        byte[] integrityHash = SecureOp.calculateHash(digest, plainText);
        plainTextWithIntegrity.write(integrityHash);
        byte[] cipherText;
        if (channelConfig.hasIV()) {
            cipherText = SecureOp.encrypt(
                    channelConfig.getCipher(),
                    ByteBuffer.wrap(plainTextWithIntegrity.toByteArray()),
                    channelConfig.getSymKey(),
                    ivSpec);
        } else {
            cipherText = SecureOp.encrypt(
                    channelConfig.getCipher(),
                    ByteBuffer.wrap(plainTextWithIntegrity.toByteArray()),
                    channelConfig.getSymKey(),
                    null);
        }
        dataStream.writeInt(cipherText.length);
        dataStream.write(cipherText);

        // Fast hash
        byte[] message = byteStream.toByteArray();
        byte[] authenticityHMAC = SecureOp.calculateHMAC(channelConfig.getMac(), channelConfig.getMacKey(), message);
        dataStream.write(authenticityHMAC);

        byte[] result = byteStream.toByteArray();
        return result;
    }

    private byte[] decode(byte[] message) throws TamperedException, InvalidKeyException, IOException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(message);
        DataInputStream dataStream = new DataInputStream(byteStream);

        byte protocolVersion = dataStream.readByte();
        if (protocolVersion > PROTO_VERSION) {
            throw new UnsupportedOperationException("Cannot handle protocol version " + protocolVersion);
        }
        String chatAddress = dataStream.readUTF();
        if (!chatAddress.equals(channelConfig.getAddress().toString())) {
            throw new UnsupportedOperationException("Chat address mismatch (" + chatAddress + ")");
        }

        byte messageType = dataStream.readByte();
        if (messageType != 0x01) {
            throw new UnsupportedOperationException("Unknown message type (" + messageType + ")");
        }
        System.out.println(byteStream.available());

        // SAttributes
        int attributeLength = channelConfig.getChatIDDigest().length;
        if (dataStream.available() < 6 * attributeLength + Integer.SIZE / Byte.SIZE) {
            throw new RuntimeException("Missing SAttributes");
        }
        byte[] attribute = dataStream.readNBytes(attributeLength);
        if (!Arrays.equals(attribute, channelConfig.getChatIDDigest())) {
            throw new RuntimeException("Mismatched channel ID");
        }
        if (!Arrays.equals(dataStream.readNBytes(attributeLength), channelConfig.getSymAlgorithmDigest())) {
            throw new RuntimeException("Mismatched cipher");
        }
        if (!Arrays.equals(dataStream.readNBytes(attributeLength), channelConfig.getModeDigest())) {
            throw new RuntimeException("Mismatched mode");
        }
        if (!Arrays.equals(dataStream.readNBytes(attributeLength), channelConfig.getPaddingDigest())) {
            throw new RuntimeException("Mismatched padding");
        }
        if (!Arrays.equals(dataStream.readNBytes(attributeLength), channelConfig.getIntegrityAlgorithmDigest())) {
            throw new RuntimeException("Mismatched integrity algorithm");
        }

        if (!Arrays.equals(dataStream.readNBytes(attributeLength), channelConfig.getMacDigest())) {
            throw new RuntimeException("Mismatched mac algorithm");
        }

        int seqNum = dataStream.readInt();
        int nounce = dataStream.readInt();
        System.out.println("SN:" + seqNum + " Nouce:" + nounce);

        //Payload
        int cipherTextLength = dataStream.readInt();
        System.out.println("CT l: " + cipherTextLength);
        if (dataStream.available() < cipherTextLength + Integer.SIZE / Byte.SIZE) {
            throw new RuntimeException("Missing payload");
        }
        byte[] cipherText = dataStream.readNBytes(cipherTextLength);
        System.out.println("Ciphertext: " + SecureOp.bytesToHex(cipherText));

        // Fast hash
        Mac mac = channelConfig.getMac();
        if (dataStream.available() < mac.getMacLength()) {
            throw new RuntimeException("Missing fast hash check");
        }
        byte[] hmac = dataStream.readNBytes(mac.getMacLength());
        if (dataStream.available() > 0) {
            throw new RuntimeException("Too much content");
        }
        System.out.println(byteStream.available());

        // Decryption
        byte[] authenticatedSegment = Arrays.copyOfRange(message, 0, message.length - mac.getMacLength());
        SecureOp.assertValidHMAC(mac, authenticatedSegment, channelConfig.getMacKey(), hmac);
        Cipher cipher = channelConfig.getCipher();
        byte[] plainTextAndIntegrity;
        if (channelConfig.hasIV()) {
            plainTextAndIntegrity = SecureOp.decrypt(
                    cipher,
                    ByteBuffer.wrap(cipherText),
                    channelConfig.getSymKey(),
                    ivSpec);
        } else {
            plainTextAndIntegrity = SecureOp.decrypt(
                    cipher,
                    ByteBuffer.wrap(cipherText),
                    channelConfig.getSymKey(),
                    null);
        }


        // Integrity check
        MessageDigest integrityDigestImpl = channelConfig.getDigest();

        byte[] plainText = Arrays.copyOfRange(
                plainTextAndIntegrity,
                0,
                plainTextAndIntegrity.length - integrityDigestImpl.getDigestLength());
        byte[] integrityDigest = Arrays.copyOfRange(
                plainTextAndIntegrity,
                plainText.length,
                plainTextAndIntegrity.length);

        System.out.println("Integrity hash: " + SecureOp.bytesToHex(integrityDigest));
        SecureOp.assertValidHash(integrityDigestImpl, plainText, integrityDigest);
        return plainText;
    }
}
