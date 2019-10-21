package view;

import controller.ChatController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Iterator;

public class ChatPane extends AnchorPane {
    protected ListView msgList;
    protected ListView peerList;
    protected Pane graphPane;
    protected ChatController controller;

    public ChatPane() {
        super();
        try {
            Parent chatUI = FXMLLoader.load(getClass().getResource("chat.fxml"));
            super.getChildren().add(chatUI);
            setTopAnchor(chatUI, 0.0);
            setBottomAnchor(chatUI, 0.0);
            setLeftAnchor(chatUI, 0.0);
            setRightAnchor(chatUI, 0.0);
            msgList = (ListView) chatUI.lookup("#msgList");
            peerList = (ListView) chatUI.lookup("#peerList");
            graphPane = (Pane) chatUI.lookup("#graphPane");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void assignController(ChatController controller) {
        this.controller = controller;
    }

    /**
     * Adiciona utilizador no interface do utilizador
     */
    protected void uiAddUser(String userName) {
    }

    /**
     * Remove utilizador no interface do utilizador.
     *
     * @return Devolve true se utilizador foi removido.
     */
    protected boolean uiRemUser(String userName) {
        // TODO
        return true;
    }

    /**
     * Inicializa lista de utilizadores a partir de um iterador -- pode ser usado
     * obtendo iterador de qualquer estrutura de dados de java
     */
    protected void uiInitUsers(Iterator it) {
        // TODO
    }

    /**
     * Devolve um Enumeration com o nome dos utilizadores que aparecem no UI.
     */
    protected Enumeration uiListUsers() {
        // TODO
        return null;
    }

    // Configuracao do grupo multicast da sessao de chat na interface do cliente
    public void join(String username, InetAddress group, int port, int ttl) throws IOException {
//        stage.setTitle("CHAT MulticastIP " + username + "@" + group.getHostAddress() + ":" + port + " [TTL=" + ttl + "]");

        // Criar sessao de chat multicast
//        chat = new MulticastChat(username, group, port, ttl, this);
    }

    protected void log(final String message) {
        java.util.Date date = new java.util.Date();

        // TODO
    }

    /**
     * Envia mensagem. Chamado quando se carrega no botao de SEND ou se faz ENTER
     * na linha da mensagem.
     * Executa operacoes relacionadas com interface -- nao modificar
     */
    protected void sendMessage() {
        // TODO
    }

    /**
     * Executa operacoes relativas ao envio de mensagens
     */
    protected void doSendMessage(String message) {
        // TODO
    }


    /**
     * Imprime mensagem de erro
     */
    protected void displayMsg(final String str, final boolean error) {
        // TODO
    }

    /**
     * Pede downlaod dum ficheiro. Chamado quando se carrega no botao de SEND ou se faz ENTER
     * na linha de download.
     * Executa operacoes relacionadas com interface -- nao modificar
     */
    protected void downloadFile() {
        // TODO
    }

    /**
     * Executa operacoes relativas ao envio de mensagens.
     * <p>
     * NOTA: Qualquer informacao ao utilizador deve ser efectuada usando
     * o metodo "displayMsg".
     */
    protected void doDownloadFile(String file) {
        // TODO: a completar
        System.err.println("Pedido download do ficheiro " + file);
    }

    /**
     * Chamado quando o utilizador fechou a janela do chat
     */
    protected void onQuit() {
        // TODO
    }


    // Invocado quando se recebe uma mensagem  //
    public void chatMessageReceived(String username, InetAddress address, int port, String message) {
        log("MSG:[" + username + "@" + address.getHostName() + "] disse: " + message);
    }


    // Invocado quando um novo utilizador se juntou ao chat  //
    public void chatParticipantJoined(String username, InetAddress address, int port) {
        log("+++ NOVO PARTICIPANTE: " + username + " juntou-se ao grupo do chat a partir de " + address.getHostName()
                + ":" + port);
    }

    // Invocado quando um utilizador sai do chat  //
    public void chatParticipantLeft(String username, InetAddress address,
                                    int port) {
        log("--- ABANDONO: " + username + " abandonou o grupo de chat, a partir de " + address.getHostName() + ":"
                + port);
    }
}
