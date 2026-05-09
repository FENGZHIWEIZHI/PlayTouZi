package com.game.dice;

import com.game.dice.model.GameSettings;
import com.game.dice.model.PlayerProfile;
import com.game.dice.ui.GameSetupScene;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 主应用程序入口
 */
public class MainApp extends Application {

    private Stage primaryStage;
    private PlayerProfile profile;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.profile = PlayerProfile.load();
        stage.setTitle("🎲 吹牛骰子 - Liar's Dice");
        stage.setResizable(true);
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        stage.setWidth(1200);
        stage.setHeight(800);

        showSetupScene();
        stage.show();
    }

    /**
     * 显示游戏设置界面
     */
    public void showSetupScene() {
        double w = primaryStage.getWidth();
        double h = primaryStage.getHeight();
        boolean maximized = primaryStage.isMaximized();
        GameSetupScene setupScene = new GameSetupScene(this, profile);
        Scene scene = setupScene.getScene();
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setWidth(w);
        primaryStage.setHeight(h);
        primaryStage.setMaximized(maximized);
    }

    /**
     * 开始游戏
     */
    public void startGame(GameSettings settings) {
        double w = primaryStage.getWidth();
        double h = primaryStage.getHeight();
        boolean maximized = primaryStage.isMaximized();
        GamePlayController gamePlay = new GamePlayController(this, settings, profile);
        Scene scene = gamePlay.createScene();
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setWidth(w);
        primaryStage.setHeight(h);
        primaryStage.setMaximized(maximized);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public PlayerProfile getProfile() {
        return profile;
    }

    public static void main(String[] args) {
        launch(args);
    }
}