package chat;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;

public class EzKeyStore {
    private KeyStore store;
    private char[] password;
    private File file;

    /**
     * Creates a keystore from scratch
     *
     * @param file     Destination
     * @param password Password
     * @return KeyHandler for the created keystore
     */
    public static EzKeyStore instantiateKeyStore(File file, String password) {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(null, password.toCharArray()); // TODO Is this needed?
            keyStore.store(new FileOutputStream(file), password.toCharArray());
            return new EzKeyStore(keyStore, password, file);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    /**
     * Generates a key with a given algorithm name
     *
     * @param algorithmName Name of the desired algorithm
     * @return Newly created key
     */
    public static SecretKey generateKey(String algorithmName, int keySize) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance(algorithmName);
        keyGen.init(keySize);
        return keyGen.generateKey();
    }

    private EzKeyStore(KeyStore store, String password, File file) {
        this.store = store;
        this.password = password.toCharArray();
        this.file = file;
    }

    /**
     * Opens up a keystore
     *
     * @param file     Location
     * @param password Password
     */
    public EzKeyStore(File file, String password) {
        this.file = file;
        if (!file.exists()){

        }
        this.password = password.toCharArray();
        try {
            store = KeyStore.getInstance("JCEKS");
            store.load(new FileInputStream(file), password.toCharArray());
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (KeyStoreException e) {
            // Doesn't happen, hopefully
        }
    }

    /**
     * Stores a key
     *
     * @param alias     Key textual alias
     * @param secretKey Key
     */
    public void storeKey(String alias, SecretKey secretKey) throws CertificateException, NoSuchAlgorithmException, KeyStoreException {
        try {
            KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(secretKey);
            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(password);
            store.setEntry(alias, keyStoreEntry, keyPassword);
            store.store(new FileOutputStream(file), password);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtains a key from its alias
     *
     * @param alias Key alias
     * @return Key
     */
    public SecretKey getKey(String alias) {
        KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection(password);
        KeyStore.Entry entry = null;
        try {
            entry = store.getEntry(alias, keyPassword);
            return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException e) {
            return null;
        }
    }
}





