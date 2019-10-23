package chat.config;

import chat.config.BadConfigException;
import chat.config.ChannelConfig;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigParser {
    private static int flags = Pattern.DOTALL;
    private static Pattern hostPattern = Pattern.compile(
            "<(?<ip>(?:\\d{1,3}\\.){3}\\d{1,3}):(?<port>\\d{1,5})>" +
                    "(?<content>.+?)" +
                    "</(?<ip2>(?:\\d{1,3}\\.){3}\\d{1,3})>",
            flags);
    private static Pattern tagPattern = Pattern.compile("<(?<tag>.+?)>(?<content>.*?)</.+?>", flags);

    public static List<ChannelConfig> from(File configFile)
            throws IOException, NoSuchPaddingException, MissingFieldException, BadConfigException, NoSuchAlgorithmException {
        ArrayList<ChannelConfig> configs = new ArrayList<>();
        InputStream is = new FileInputStream(configFile);
        byte[] fileContents = is.readAllBytes();
        String configString = new String(fileContents, StandardCharsets.UTF_8);
        Matcher match = hostPattern.matcher(configString);
        while (match.find()) {
            String ip = match.group("ip");
            String ipCheck = match.group("ip");
            int port = Integer.parseInt(match.group("port"));
            if (!ip.equals(ipCheck)) {
                throw new BadConfigException("IP mismatch in tag pairs <" + ip + "> and <" + ipCheck + ">");
            }
            if (port > 65535) {
                throw new BadConfigException("Port " + port + " isn't a valid port");
            }
            InetAddress address = Inet4Address.getByName(ip);
            if (!address.isMulticastAddress()) {
                throw new RuntimeException("Invalid multicast IP " + address);
            }
            InetSocketAddress socketAddress = new InetSocketAddress(address, port);
            String content = match.group("content");
            Matcher innerMatch = tagPattern.matcher(content);
            ChannelConfig channelConfig = new ChannelConfig(socketAddress);
            while (innerMatch.find()) {
                try {
                    switch (innerMatch.group("tag")) {
                        case "SID":
                            channelConfig.chatID = innerMatch.group("content");
                            break;
                        case "SEA":
                            channelConfig.symmetricAlgorithm = innerMatch.group("content");
                            break;
                        case "SEAKS":
                            channelConfig.symmetricKeySize = Integer.parseInt(innerMatch.group("content"));
                            break;
                        case "MODE":
                            channelConfig.mode = innerMatch.group("content");
                            break;
                        case "PADDING":
                            channelConfig.paddingAlgorithm = innerMatch.group("content");
                            break;
                        case "INTHASH":
                            channelConfig.integrityHash = innerMatch.group("content");
                            break;
                        case "MAC":
                            channelConfig.macAlgorithm = innerMatch.group("content");
                            break;
                        case "MACKS":
                            channelConfig.macKeySize = Integer.parseInt(innerMatch.group("content"));
                            break;
                        default:
                            // TODO Ó palhaço!
                    }
                } catch (NumberFormatException e) {
                    throw new BadConfigException("Unable to parse integer in the configuration of " + socketAddress);
                }
            }
            channelConfig.loadAlgorithms();
            configs.add(channelConfig);
        }
        return configs;
    }
}
