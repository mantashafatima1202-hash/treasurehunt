package game;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class GameMenu {

    public static Scene create(App app) {
        Text title = new Text("Select a game to play");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");

        Button TreasureHuntButton = new Button("TreasureHunt");
        TreasureHuntButton.setPrefWidth(200);
        TreasureHuntButton.setOnAction(e -> app.showTreasureHunt());

        VBox layout = new VBox(20, title, TreasureHuntButton);
        layout.setAlignment(Pos.CENTER);

        return new Scene(layout, 800, 600);
    }
}

