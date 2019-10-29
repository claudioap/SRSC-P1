package view;

import controller.AppController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;

public class ChatClient extends Application {
    protected AppController controller;
    protected Scene scene;
    protected Stage stage;
    protected TextField sendField;
    protected Button sendBtn;
    protected TabPane tabPane;


    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        Parent root = FXMLLoader.load(getClass().getResource("ui.fxml"));
        scene = new Scene(root, 800, 600);
        stage.setTitle("Super secure chat!");
        stage.setScene(scene);
        sendField = (TextField) scene.lookup("#sendField");
        sendBtn = (Button) scene.lookup("#sendBtn");
        tabPane = (TabPane) scene.lookup("#tabPane");
        controller = new AppController(this);
        sendBtn.setOnAction((button) -> onSendInputAction());
        sendField.setOnAction((field) -> onSendInputAction());
        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (ov, oldTab, newTab) -> controller.changeCurrentChat(newTab.getId())
        );
        stage.show();
    }

    @Override
    public void stop(){
        controller.shutdown();
    }

    public void addChatPane(String tabId, String title, ChatPane chatPane) {
        Tab tab = new Tab();
        tab.setId(tabId);
        tab.setText(title);
        tab.setContent(chatPane);
        tabPane.getTabs().add(tab);
    }

    private void onSendInputAction() {
        String message = sendField.getText();
        sendField.setText("");
        if (!"".equals(message)) {
            controller.sendMessage(message);
        }
    }

    public String promptUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Identifique-se perante as autoridades!");
        dialog.setHeaderText("Indique o seu nome");
        dialog.setContentText("Nome:");
        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                return result.get();
            }
        }
    }

    public String promptPassword() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("FBI, OPEN UP!");
        HBox headerContent = new HBox();
        Image image = new Image(getClass().getResource("header.png").toString());
        ImageView imageView = new ImageView(image);
        headerContent.getChildren().add(imageView);
        dialog.setHeaderText(" ");
        dialog.setGraphic(imageView);
        ButtonType okType = new ButtonType("Falar com um agente");
        dialog.getDialogPane().getButtonTypes().add(okType);

        PasswordField pwd = new PasswordField();
        pwd.setText(" ");
        pwd.setPrefWidth(240);
        VBox vContent = new VBox();
        image = new Image(getClass().getResource("banks.png").toString());
        imageView = new ImageView(image);
        imageView.setFitHeight(40);
        imageView.setPreserveRatio(true);

        HBox content = new HBox();
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        content.getChildren().addAll(new Label("Insira o pin para corrigir:"), pwd, imageView);

        vContent.getChildren().add(content);
        dialog.getDialogPane().setContent(vContent);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okType) {
                return pwd.getText();
            }
            return null;
        });

        while (true) {
            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                return result.get();
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
