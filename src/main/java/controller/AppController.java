package controller;

import chat.config.BadConfigException;
import chat.config.ChannelConfig;
import chat.EzKeyStore;
import chat.config.ConfigParser;
import chat.config.MissingFieldException;
import view.ChatClient;
import view.ChatPane;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

public class AppController {
    private List<ChannelConfig> configs;
    private ChatClient view;
    private ArrayList<ChatController> chatControllers = new ArrayList<>();
    private EzKeyStore keyHandler;

    public AppController(ChatClient view) {
        this.view = view;
        String username = view.promptUsername();
        String password = view.promptPassword();
        keyHandler = new EzKeyStore(new File("./conf/keystore"), password);
        try {
            for (ChannelConfig config : ConfigParser.from(new File("./conf/SMCP.conf"))) {
                SecretKey symmetric = keyHandler.getKey(config.hashedIdentifier() + "-key");
                if (symmetric == null) {
                    System.out.println("Generating symmetric key");
                    symmetric = EzKeyStore.generateKey(config.getSymmetricAlgorithm(), config.getSymmetricKeySize());
                    keyHandler.storeKey(config.hashedIdentifier() + "-key", symmetric);
                }
                SecretKey mac = keyHandler.getKey(config.hashedIdentifier() + "-mac");
                if (mac == null) {
                    System.out.println("Generating mac key");
                    mac = EzKeyStore.generateKey(config.getMacAlgorithm(), config.getMacKeySize());
                    keyHandler.storeKey(config.hashedIdentifier() + "-mac", mac);
                }
                config.attachKeys(symmetric, mac);
                ChatPane chatPane = new ChatPane();
                ChatController chatController = new ChatController(this, chatPane, config, username);
                view.addChatPane(config.getChatID(), chatPane);
                chatControllers.add(chatController);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.err.println("Configuration missing");
            System.exit(-1);
        } catch (CertificateException | KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadConfigException e) {
            e.printStackTrace();
        } catch (MissingFieldException e) {
            e.printStackTrace();
        }
    }
}
