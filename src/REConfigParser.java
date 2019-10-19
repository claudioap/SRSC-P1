import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class REConfigParser {
    private static int flags = Pattern.DOTALL;
    private static Pattern hostPattern = Pattern.compile(
            "<(?<ip>(?:\\d{1,3}\\.){3}\\d{1,3}):(?<port>\\d{1,5})>" +
                    "(?<content>.+?)" +
                    "</(?<ip2>(?:\\d{1,3}\\.){3}\\d{1,3})>",
            flags);
    private static Pattern tagPattern = Pattern.compile("<(?<tag>.+?)>(?<content>.*?)</.+?>", flags);
    private ArrayList<ChannelConfig> configs = new ArrayList<>();

    public REConfigParser(File configFile) throws IOException {
        InputStream is = new FileInputStream(configFile);
        byte[] fileContents = is.readAllBytes();
        String configString = new String(fileContents, StandardCharsets.UTF_8);
        Matcher match = hostPattern.matcher(configString);
        while (match.find()) {
            String ip = match.group("ip");
            String ipCheck = match.group("ip");
            int port = Integer.parseInt(match.group("port"));
            if (!ip.equals(ipCheck)) {
                // Ó palhaço!
            }
            if (port > 65535) {
                // Ó palhaço!
            }
            InetAddress address = Inet4Address.getByName(ip);
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
                            // Ó palhaço!
                    }
                } catch (NumberFormatException e) {
                    // Ó palhaço!
                }
            }
            if (!channelConfig.isComplete()) {
                // Ó palhaço!
            }
            configs.add(channelConfig);
        }
    }

    class ChannelConfig {
        public ChannelConfig(InetSocketAddress address) {
            this.address = address;
        }

        private InetSocketAddress address;
        private String chatID;
        private String symmetricAlgorithm;
        private Integer symmetricKeySize;
        private String mode;
        private String paddingAlgorithm;
        private String integrityHash;
        private String macAlgorithm;
        private Integer macKeySize;

        private boolean isComplete() {
            return !(chatID == null
                    || symmetricAlgorithm == null
                    || symmetricKeySize == null
                    || mode == null
                    || paddingAlgorithm == null
                    || integrityHash == null
                    || macAlgorithm == null
                    || macKeySize == null);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Addr=").append(address).append(";");
            builder.append("SID=").append(chatID).append(";");
            builder.append("SymAlg=").append(symmetricAlgorithm).append(";");
            builder.append("SymKS=").append(symmetricKeySize).append(";");
            builder.append("Mode=").append(mode).append(";");
            builder.append("Hash=").append(integrityHash).append(";");
            builder.append("MAC=").append(macAlgorithm).append(";");
            builder.append("MACKS=").append(macKeySize).append(";");
            return builder.toString();
        }
    }
}
