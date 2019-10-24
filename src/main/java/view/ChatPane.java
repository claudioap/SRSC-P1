package view;

import controller.ChatController;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import java.io.IOException;
import java.util.Map;

public class ChatPane extends AnchorPane {
    protected GridPane msgGrid = null;
    protected ListView peerList = null;
    protected Pane graphPane = null;

    protected ChatController controller;

    public ChatPane() {
        super();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("chat.fxml"));
            Node chatUI = loader.load();
            Map<String, Object> namespace = loader.getNamespace();

            getChildren().add(chatUI);
            setTopAnchor(chatUI, 0.0);
            setBottomAnchor(chatUI, 0.0);
            setLeftAnchor(chatUI, 0.0);
            setRightAnchor(chatUI, 0.0);
            msgGrid = (GridPane) namespace.get("msgGrid");
            peerList = (ListView) namespace.get("peerList");
            graphPane = (Pane) namespace.get("graphPane");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(String author, String content, SpeechAuthor direction) {
        int row = msgGrid.getRowCount();
//        Text authorText = new Text(localAuthor);
        UserSymbol userSymbol = new UserSymbol(author);
        Text placeholder = new Text("");
        SpeechBox speechBox = new SpeechBox(content, direction);
        if (direction == null) {
            Text placeholder2 = new Text("");
            msgGrid.addRow(row, placeholder2, speechBox, placeholder);
        } else if (direction == SpeechAuthor.SELF) {
            msgGrid.addRow(row, placeholder, speechBox, userSymbol);
        } else {
            msgGrid.addRow(row, userSymbol, speechBox, placeholder);
        }
        GridPane.setValignment(userSymbol, VPos.TOP);
        GridPane.setHgrow(speechBox, Priority.ALWAYS);
    }

    public void assignController(ChatController controller) {
        this.controller = controller;
    }

    public void addUser(String username) {
        if (!this.peerList.getItems().contains(username)) {
            this.peerList.getItems().add(username);
        }
    }

    public void removeUser(String username) {
        ObservableList peerList = this.peerList.getItems();
        peerList.remove(username);
    }
}
