package game;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        stage.setTitle("DIT 2026 Project");

        stage.setResizable(false);

        stage.setWidth(1380);
        stage.setHeight(780);
        stage.setMinWidth(1380);
        stage.setMinHeight(780);
        stage.setMaxWidth(1380);
        stage.setMaxHeight(780);

        showMainMenu();
        stage.show();
    }


    public void showMainMenu() {
        stage.setScene(MainMenu.create(this));
    }

    public void showGameSelect() {
        stage.setScene(GameMenu.create(this));
    }

    public void showTreasureHunt() {
        stage.setScene(TreasureHunt.create(this));
    }
}
