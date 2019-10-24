package chat.networking;

import chat.SecureOp;

public class TamperedException extends Exception {
    public TamperedException(byte[] expected, byte[] ocurred) {
        super("Expected: " + SecureOp.bytesToHex(expected) + "\tGot: " + SecureOp.bytesToHex(ocurred));
    }
}
