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
        gc.fillText(name.substring(0, 3), 5, 25);
    }
}
