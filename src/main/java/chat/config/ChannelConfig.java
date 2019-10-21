package chat.config;

import chat.SecureOp;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import java.net.InetSocketAddress;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChannelConfig {
    public ChannelConfig(InetSocketAddress address) {
        this.address = address;
    }

    protected InetSocketAddress address;
    protected String chatID;
    protected String symmetricAlgorithm;
    protected Key symmetricKey;
    protected Integer symmetricKeySize;
    protected String mode;
    protected String paddingAlgorithm;
    protected String integrityHash;
    protected String macAlgorithm;
    protected Integer macKeySize;
    protected Key macKey;
    protected Cipher cipher;
    protected Mac mac;
    protected MessageDigest digest;

    public InetSocketAddress getAddress() {
        return address;
    }

    public String getChatID() {
        return chatID;
    }

    public String getSymmetricAlgorithm() {
        return symmetricAlgorithm;
    }

    public Integer getSymmetricKeySize() {
        return symmetricKeySize;
    }

    public String getMacAlgorithm() {
        return macAlgorithm;
    }

    public Integer getMacKeySize() {
        return macKeySize;
    }

    public String getMode() {
        return mode;
    }

    public String getPaddingAlgorithm() {
        return paddingAlgorithm;
    }

    public String getIntegrityHash() {
        return integrityHash;
    }

    public void loadAlgorithms() throws NoSuchAlgorithmException, NoSuchPaddingException, MissingFieldException {
        if (chatID == null) {
            throw new MissingFieldException("chatID is missing");
        }
        if (symmetricAlgorithm == null) {
            throw new MissingFieldException("symmetricAlgorithm is missing");
        }
        if (symmetricKeySize == null) {
            throw new MissingFieldException("symmetricKeySize is missing");
        }
        if (mode == null) {
            throw new MissingFieldException("mode is missing");
        }
        if (paddingAlgorithm == null) {
            throw new MissingFieldException("paddingAlgorithm is missing");
        }
        if (integrityHash == null) {
            throw new MissingFieldException("integrityHash is missing");
        }
        if (macAlgorithm == null) {
            throw new MissingFieldException("macAlgorithm is missing");
        }
        if (macKeySize == null) {
            throw new MissingFieldException("macKeySize is missing");
        }
        cipher = Cipher.getInstance(symmetricAlgorithm + "/" + mode + "/" + paddingAlgorithm);
        mac = Mac.getInstance(macAlgorithm);
        digest = MessageDigest.getInstance(integrityHash);
    }

    public void attachKeys(Key symmetricKey, Key macKey) {
        this.symmetricKey = symmetricKey;
        this.macKey = macKey;
    }

    public Key getSymmetricKey() {
        return symmetricKey;
    }

    public Key getMacKey() {
        return macKey;
    }

    public Cipher getCipher() {
        return cipher;
    }

    public Mac getMac() {
        return mac;
    }

    public MessageDigest getDigest() {
        return digest;
    }

    @Override
    public String toString() {
        return "Addr=" + address + ";" +
                "SID=" + chatID + ";" +
                "SymAlg=" + symmetricAlgorithm + ";" +
                "SymKS=" + symmetricKeySize + ";" +
                "Mode=" + mode + ";" +
                "Hash=" + integrityHash + ";" +
                "MAC=" + macAlgorithm + ";" +
                "MACKS=" + macKeySize + ";";
    }

    public String hashedIdentifier() {
        try {
            return SecureOp.bytesToHex(
                    SecureOp.calculateHash(
                            MessageDigest.getInstance("SHA-1"),
                            getChatID().getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }
}
