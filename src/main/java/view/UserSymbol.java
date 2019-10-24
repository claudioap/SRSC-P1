package view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class UserSymbol extends Canvas {
    public UserSymbol(String name) {
        super(40, 40);
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.DARKBLUE);
        gc.fillOval(0, 0, 40, 40);
        gc.setFill(Color.WHITESMOKE);
        String[] words = name.split(" ");
        String text;
        if (words.length == 1) {
            if (name.length() < 3) {
                text = name;
            } else {
                text = name.substring(0, 3);
            }
        } else {
            text = "" + words[0].charAt(0) + words[words.length - 1].charAt(0);
        }
        gc.fillText(text, 5, 25);
    }
}
