package game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class MainMenu {

    public static Scene create(App app) {

        Font.loadFont(MainMenu.class.getResourceAsStream("/fonts/VT323-Regular.ttf"), 20);

        Text title = new Text("Treasure Hunt - CSN206");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-font: VT323");

        Button startButton = new Button("Start");
        startButton.setFont(Font.font("VT323", 25));
        startButton.setTextFill(Color.WHITE);
        startButton.setStyle("-fx-background-color: #4CAF50;");
        startButton.setPrefSize(90, 60);
        startButton.setOnAction(e -> app.showGameSelect());

        add3DEffect(startButton);

        HBox buttonLayout = new HBox(40, startButton);
        VBox layout = new VBox(30, title, buttonLayout);
        buttonLayout.setAlignment(Pos.CENTER);
        layout.setAlignment(Pos.CENTER);

        return new Scene(layout, 800, 600);
    }

    private static void add3DEffect(Button button) {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10);
        shadow.setOffsetY(4);
        shadow.setColor(Color.rgb(0, 0, 0, 0.4));

        button.setEffect(shadow);

        String originalStyle = button.getStyle();

        button.setOnMouseEntered(e -> {
            shadow.setOffsetY(2);
            button.setScaleX(1.08);
            button.setScaleY(1.08);

            button.setStyle(originalStyle + "; -fx-background-color: derive(" +
                    extractColor(originalStyle) + ", 40%);");
        });

        button.setOnMouseExited(e -> {
            shadow.setOffsetY(4);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
            button.setStyle(originalStyle);
        });

        button.setOnMousePressed(e -> {
            shadow.setOffsetY(1);
            button.setScaleX(0.97);
            button.setScaleY(0.97);
        });

        button.setOnMouseReleased(e -> {
            shadow.setOffsetY(2);
            button.setScaleX(1.08);
            button.setScaleY(1.08);
        });
    }

    private static String extractColor(String style) {
        int start = style.indexOf("#");
        int end = style.indexOf(";", start);
        return style.substring(start, end);
    }
}

