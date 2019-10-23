package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;


public class SpeechBox extends HBox {
    private Color DEFAULT_SENDER_COLOR = Color.LIGHTBLUE;
    private Color DEFAULT_RECEIVER_COLOR = Color.LIGHTGRAY;
    private Background DEFAULT_SENDER_BACKGROUND, DEFAULT_RECEIVER_BACKGROUND;

    private String message;
    private SpeechAuthor direction;

    private Label displayedText;
    private SVGPath directionIndicator;

    public SpeechBox(String message, SpeechAuthor direction) {
        this.message = message;
        this.direction = direction;
        initDefaults();
        setupElements();
    }

    private void initDefaults() {
        DEFAULT_SENDER_BACKGROUND = new Background(
                new BackgroundFill(
                        DEFAULT_SENDER_COLOR,
                        new CornerRadii(5, 0, 5, 5, false),
                        Insets.EMPTY));
        DEFAULT_RECEIVER_BACKGROUND = new Background(
                new BackgroundFill(
                        DEFAULT_RECEIVER_COLOR,
                        new CornerRadii(0, 5, 5, 5, false),
                        Insets.EMPTY));
    }

    private void setupElements() {
        displayedText = new Label(message);
        displayedText.setPadding(new Insets(5));
        displayedText.setWrapText(true);
        directionIndicator = new SVGPath();


        switch (direction) {
            case SELF:
                displayedText.setBackground(DEFAULT_SENDER_BACKGROUND);
                displayedText.setAlignment(Pos.CENTER_RIGHT);
                directionIndicator.setContent("M10 0 L0 10 L0 0 Z");
                directionIndicator.setFill(DEFAULT_SENDER_COLOR);
                configureForSender();
                break;
            case OTHER:
                displayedText.setBackground(DEFAULT_RECEIVER_BACKGROUND);
                displayedText.setAlignment(Pos.CENTER_LEFT);
                directionIndicator.setContent("M0 0 L10 0 L10 10 Z");
                directionIndicator.setFill(DEFAULT_RECEIVER_COLOR);
                configureForReceiver();
                break;
            default:
                displayedText.setAlignment(Pos.CENTER);
        }
    }

    private void configureForSender() {

        HBox container = new HBox(displayedText, directionIndicator);
        //Use at most 75% of the width provided to the SpeechBox for displaying the message
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_RIGHT);
    }

    private void configureForReceiver() {

        HBox container = new HBox(directionIndicator, displayedText);
        //Use at most 75% of the width provided to the SpeechBox for displaying the message
        container.maxWidthProperty().bind(widthProperty().multiply(0.75));
        getChildren().setAll(container);
        setAlignment(Pos.CENTER_LEFT);
    }
}