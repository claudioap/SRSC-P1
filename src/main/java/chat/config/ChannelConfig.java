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

    protected InetSocketAddress address;
    protected String chatID;
    protected String symAlgorithm;
    protected Key symKey;
    protected Integer symKeySize;
    protected String mode;
    protected String paddingAlgorithm;
    protected String integrityAlgorithm;
    protected String macAlgorithm;
    protected Integer macKeySize;
    protected Key macKey;
    protected Cipher cipher;
    protected Mac mac;
    protected MessageDigest digest;
    protected boolean hasIV = false;

    private MessageDigest identifierDigest;
    // Cached fields calculated from identifierDigest
    protected String chatDigest;
    protected byte[] chatIDDigest;
    protected byte[] symAlgorithmDigest;
    protected byte[] modeDigest;
    protected byte[] paddingDigest;
    protected byte[] macDigest;
    protected byte[] integrityAlgorithmDigest;

    public ChannelConfig(InetSocketAddress address) {
        try {
            identifierDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available");
        }
        this.address = address;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public String getChatID() {
        return chatID;
    }

    public String getSymAlgorithm() {
        return symAlgorithm;
    }

    public Integer getSymKeySize() {
        return symKeySize;
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

    public String getIntegrityAlgorithm() {
        return integrityAlgorithm;
    }

    void loadAlgorithms() throws NoSuchAlgorithmException, NoSuchPaddingException, MissingFieldException {
        if (chatID == null) {
            throw new MissingFieldException("chatID is missing");
        }
        if (symAlgorithm == null) {
            throw new MissingFieldException("symmetricAlgorithm is missing");
        }
        if (symKeySize == null) {
            throw new MissingFieldException("symmetricKeySize is missing");
        }
        if (mode == null) {
            throw new MissingFieldException("mode is missing");
        }
        if (paddingAlgorithm == null) {
            throw new MissingFieldException("paddingAlgorithm is missing");
        }
        if (integrityAlgorithm == null) {
            throw new MissingFieldException("integrityHash is missing");
        }
        if (macAlgorithm == null) {
            throw new MissingFieldException("macAlgorithm is missing");
        }
        if (macKeySize == null) {
            throw new MissingFieldException("macKeySize is missing");
        }
        switch (symAlgorithm) {
            case "AES":
                cipher = Cipher.getInstance(symAlgorithm + "/" + mode + "/" + paddingAlgorithm);
                switch (mode) {
                    case "CTR":
                        hasIV = true;
                        break;
                    case "ECB":
                        hasIV = false;
                        break;
                    default:
                        throw new RuntimeException("Unsupported mode AES/" + mode);
                }
                break;
            case "Blowfish":
            case "RC4":
                cipher = Cipher.getInstance(symAlgorithm);
                break;
            default:
                throw new RuntimeException("Unsupported cipher " + symAlgorithm);

        }
        mac = Mac.getInstance(macAlgorithm);
        digest = MessageDigest.getInstance(integrityAlgorithm);
    }

    public void attachKeys(Key symmetricKey, Key macKey) {
        this.symKey = symmetricKey;
        this.macKey = macKey;
    }

    public Key getSymKey() {
        return symKey;
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

    public boolean hasIV() {
        return hasIV;
    }

    @Override
    public String toString() {
        return "Addr=" + address + ";" +
                "SID=" + chatID + ";" +
                "SymAlg=" + symAlgorithm + ";" +
                "SymKS=" + symKeySize + ";" +
                "Mode=" + mode + ";" +
                "Hash=" + integrityAlgorithm + ";" +
                "MAC=" + macAlgorithm + ";" +
                "MACKS=" + macKeySize + ";";
    }

    public byte[] getChatIDDigest() {
        if (chatIDDigest == null) {
            chatIDDigest = SecureOp.calculateHash(identifierDigest, getChatID().getBytes());
        }
        return chatIDDigest;
    }

    public String getChatIDDigestString() {
        return SecureOp.bytesToHex(getChatIDDigest());
    }

    public byte[] getSymAlgorithmDigest() {
        if (symAlgorithmDigest == null) {
            symAlgorithmDigest = SecureOp.calculateHash(identifierDigest, symAlgorithm.getBytes());
        }
        return symAlgorithmDigest;
    }

    public byte[] getModeDigest() {
        if (modeDigest == null) {
            modeDigest = SecureOp.calculateHash(identifierDigest, mode.getBytes());
        }
        return modeDigest;
    }

    public byte[] getPaddingDigest() {
        if (paddingDigest == null) {
            paddingDigest = SecureOp.calculateHash(identifierDigest, paddingAlgorithm.getBytes());
        }
        return paddingDigest;
    }

    public byte[] getMacDigest() {
        if (macDigest == null) {
            macDigest = SecureOp.calculateHash(identifierDigest, macAlgorithm.getBytes());
        }
        return macDigest;
    }

    public byte[] getIntegrityAlgorithmDigest() {
        if (integrityAlgorithmDigest == null) {
            integrityAlgorithmDigest = SecureOp.calculateHash(identifierDigest, integrityAlgorithm.getBytes());
        }
        return integrityAlgorithmDigest;
    }
}
