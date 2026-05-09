package com.game.dice.ui;

import com.game.dice.MainApp;
import com.game.dice.model.GameSettings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * 游戏设置界面
 */
public class GameSetupScene {

    private final MainApp app;
    private final GameSettings settings;

    public GameSetupScene(MainApp app) {
        this.app = app;
        this.settings = new GameSettings();
    }

    public Scene getScene() {
        // 标题
        Text title = new Text("🎲 吹牛骰子");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 48));
        title.setFill(Color.WHITE);
        title.setEffect(new DropShadow(10, Color.BLACK));

        Text subtitle = new Text("Liar's Dice");
        subtitle.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 24));
        subtitle.setFill(Color.LIGHTYELLOW);

        VBox titleBox = new VBox(5, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // 设置面板
        VBox settingsBox = new VBox(15);
        settingsBox.setPadding(new Insets(25));
        settingsBox.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 15;");
        settingsBox.setMaxWidth(450);

        // 玩家名称
        Label nameLabel = createLabel("玩家名称:");
        TextField nameField = new TextField("玩家");
        nameField.setPromptText("输入你的名字");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        settings.setHumanPlayerName("玩家");
        nameField.textProperty().addListener((obs, old, val) -> settings.setHumanPlayerName(val));

        // 玩家数量
        Label playerLabel = createLabel("玩家数量 (2-6):");
        Spinner<Integer> playerSpinner = new Spinner<>(2, 6, 4);
        playerSpinner.setPrefWidth(120);
        playerSpinner.setStyle("-fx-font-size: 14px;");
        playerSpinner.valueProperty().addListener((obs, old, val) -> settings.setPlayerCount(val));

        // 游戏模式
        Label modeLabel = createLabel("游戏模式:");
        ToggleGroup modeGroup = new ToggleGroup();
        VBox modeBox = new VBox(8);
        for (GameSettings.GameMode mode : GameSettings.GameMode.values()) {
            RadioButton rb = new RadioButton(mode.getDisplayName());
            rb.setToggleGroup(modeGroup);
            rb.setUserData(mode);
            rb.setTextFill(Color.WHITE);
            rb.setFont(Font.font("Microsoft YaHei", 14));
            if (mode == GameSettings.GameMode.TARGET_SCORE) rb.setSelected(true);
            modeBox.getChildren().add(rb);
        }

        // 模式参数
        Label paramLabel = createLabel("目标分数:");
        Spinner<Integer> paramSpinner = new Spinner<>(5, 200, 20);
        paramSpinner.setPrefWidth(120);
        paramSpinner.setStyle("-fx-font-size: 14px;");

        modeGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            if (val != null) {
                GameSettings.GameMode mode = (GameSettings.GameMode) val.getUserData();
                settings.setGameMode(mode);
                switch (mode) {
                    case TARGET_SCORE -> {
                        paramLabel.setText("目标分数:");
                        paramSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 200, 20));
                    }
                    case FIXED_ROUNDS -> {
                        paramLabel.setText("固定局数:");
                        paramSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(3, 50, 10));
                    }
                    case ELIMINATION -> {
                        paramLabel.setText("初始分数:");
                        paramSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 50, 20));
                    }
                }
            }
        });

        paramSpinner.valueProperty().addListener((obs, old, val) -> {
            switch (settings.getGameMode()) {
                case TARGET_SCORE -> settings.setTargetScore(val);
                case FIXED_ROUNDS -> settings.setFixedRounds(val);
                case ELIMINATION -> settings.setEliminationStartScore(val);
            }
        });

        // 开始按钮
        Button startBtn = new Button("🎮 开始游戏");
        startBtn.setStyle("-fx-font-size: 18px; -fx-padding: 12 40; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;");
        startBtn.setOnMouseEntered(e -> startBtn.setStyle("-fx-font-size: 18px; -fx-padding: 12 40; -fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));
        startBtn.setOnMouseExited(e -> startBtn.setStyle("-fx-font-size: 18px; -fx-padding: 12 40; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-cursor: hand;"));
        startBtn.setOnAction(e -> app.startGame(settings));

        HBox btnBox = new HBox(startBtn);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(10, 0, 0, 0));

        settingsBox.getChildren().addAll(
                nameLabel, nameField,
                playerLabel, playerSpinner,
                modeLabel, modeBox,
                paramLabel, paramSpinner,
                btnBox
        );

        // 规则提示
        Text rulesText = new Text(
            "规则简介：\n" +
            "• 每人5颗骰子，轮流叫牌\n" +
            "• 叫牌必须比上家更高（数量更多或同数量点数更大）\n" +
            "• 1点是万能牌，可以代表任何点数\n" +
            "• 不相信上家可以喊「开」，所有人亮骰子验证"
        );
        rulesText.setFont(Font.font("Microsoft YaHei", 13));
        rulesText.setFill(Color.LIGHTYELLOW);
        rulesText.setStyle("-fx-line-spacing: 4;");

        VBox rulesBox = new VBox(rulesText);
        rulesBox.setPadding(new Insets(15));
        rulesBox.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10;");
        rulesBox.setMaxWidth(450);

        // 主布局
        VBox mainBox = new VBox(25, titleBox, settingsBox, rulesBox);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(30));
        mainBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e, #0f3460);");
        VBox.setVgrow(mainBox, Priority.ALWAYS);

        StackPane root = new StackPane(mainBox);
        root.setStyle("-fx-background-color: #1a1a2e;");

        Scene scene = new Scene(root);
        return scene;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 15));
        label.setTextFill(Color.WHITESMOKE);
        return label;
    }
}