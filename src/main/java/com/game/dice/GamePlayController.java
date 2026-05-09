package com.game.dice;

import com.game.dice.engine.GameEngine;
import com.game.dice.model.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.*;

/**
 * 游戏主界面控制器
 */
public class GamePlayController {

    private final MainApp app;
    private final GameSettings settings;
    private GameEngine engine;

    // UI 组件
    private HBox playersContainer;
    private VBox logBox;
    private ScrollPane logScrollPane;
    private HBox bidControlBox;
    private Spinner<Integer> countSpinner;
    private Spinner<Integer> faceSpinner;
    private Button bidButton;
    private Button challengeButton;
    private Label turnLabel;
    private Label roundLabel;
    private Label currentBidLabel;
    private VBox mainLayout;
    private Map<String, VBox> playerPanelMap;
    private Label timerLabel;
    private boolean showAllDice = false;

    public GamePlayController(MainApp app, GameSettings settings) {
        this.app = app;
        this.settings = settings;
        this.playerPanelMap = new LinkedHashMap<>();
    }

    public Scene createScene() {
        engine = new GameEngine(settings);
        setupCallbacks();

        // 顶部信息栏
        HBox topBar = createTopBar();

        // 玩家面板区域
        playersContainer = new HBox(10);
        playersContainer.setPadding(new Insets(10));
        playersContainer.setAlignment(Pos.CENTER);

        // 用ScrollPane包裹玩家面板，支持横向滚动
        ScrollPane playersScroll = new ScrollPane(playersContainer);
        playersScroll.setFitToWidth(true);
        playersScroll.setFitToHeight(true);
        playersScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        playersScroll.setPrefHeight(250);

        // 中间区域：玩家面板
        VBox centerArea = new VBox();
        centerArea.getChildren().add(playersScroll);
        VBox.setVgrow(playersScroll, Priority.ALWAYS);

        // 底部操作区
        VBox bottomArea = createBottomArea();

        // 右侧日志区
        VBox logArea = createLogArea();

        // 主布局
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centerArea);
        root.setBottom(bottomArea);
        root.setRight(logArea);
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e, #0f3460);");
        BorderPane.setMargin(centerArea, new Insets(5));
        BorderPane.setMargin(bottomArea, new Insets(5, 5, 5, 5));
        BorderPane.setMargin(logArea, new Insets(5, 5, 5, 0));

        // 让日志区域宽度随窗口缩放，但有最小/最大限制
        logArea.minWidthProperty().bind(root.widthProperty().multiply(0.2));
        logArea.maxWidthProperty().bind(root.widthProperty().multiply(0.35));
        logArea.prefWidthProperty().bind(root.widthProperty().multiply(0.25));

        Scene scene = new Scene(root);

        // 延迟启动游戏
        Platform.runLater(() -> {
            engine.startGame();
            refreshPlayerPanels();
        });

        return scene;
    }

    /**
     * 创建顶部信息栏
     */
    private HBox createTopBar() {
        roundLabel = new Label("回合: 0");
        roundLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        roundLabel.setTextFill(Color.GOLD);

        turnLabel = new Label("等待开始...");
        turnLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        turnLabel.setTextFill(Color.WHITESMOKE);

        currentBidLabel = new Label("当前叫牌: 无");
        currentBidLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        currentBidLabel.setTextFill(Color.LIGHTCYAN);

        timerLabel = new Label("");
        timerLabel.setFont(Font.font("Microsoft YaHei", 14));
        timerLabel.setTextFill(Color.ORANGE);

        Button showDiceBtn = new Button("👁 查看所有骰子");
        showDiceBtn.setStyle("-fx-font-size: 12px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        showDiceBtn.setOnAction(e -> {
            showAllDice = !showAllDice;
            showDiceBtn.setText(showAllDice ? "🙈 隐藏骰子" : "👁 查看所有骰子");
            refreshPlayerPanels();
        });

        Button backBtn = new Button("🏠 返回");
        backBtn.setStyle("-fx-font-size: 12px; -fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5;");
        backBtn.setOnAction(e -> app.showSetupScene());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(20, roundLabel, turnLabel, currentBidLabel, timerLabel, spacer, showDiceBtn, backBtn);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 0 0 10 10;");
        return topBar;
    }

    /**
     * 创建游戏信息区域（当前叫牌详情）
     */
    private HBox createGameInfoArea() {
        HBox infoBox = new HBox(20);
        infoBox.setPadding(new Insets(5, 15, 5, 15));
        infoBox.setAlignment(Pos.CENTER);
        infoBox.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-background-radius: 8;");
        return infoBox;
    }

    /**
     * 创建底部操作区域
     */
    private VBox createBottomArea() {
        // 叫牌控制
        Label countLabel = new Label("数量:");
        countLabel.setTextFill(Color.WHITE);
        countLabel.setFont(Font.font("Microsoft YaHei", 14));

        countSpinner = new Spinner<>(1, 30, 1);
        countSpinner.setPrefWidth(90);
        countSpinner.setStyle("-fx-font-size: 14px;");

        Label faceLabel = new Label("点数:");
        faceLabel.setTextFill(Color.WHITE);
        faceLabel.setFont(Font.font("Microsoft YaHei", 14));

        faceSpinner = new Spinner<>(1, 6, 2);
        faceSpinner.setPrefWidth(90);
        faceSpinner.setStyle("-fx-font-size: 14px;");

        bidButton = new Button("📢 叫牌");
        bidButton.setStyle("-fx-font-size: 16px; -fx-padding: 10 30; -fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        bidButton.setOnAction(e -> handleBid());
        bidButton.setDisable(true);

        challengeButton = new Button("🔍 开牌");
        challengeButton.setStyle("-fx-font-size: 16px; -fx-padding: 10 30; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        challengeButton.setOnAction(e -> handleChallenge());
        challengeButton.setDisable(true);

        // 快捷叫牌按钮区
        HBox quickBidBox = createQuickBidBox();

        bidControlBox = new HBox(15, countLabel, countSpinner, faceLabel, faceSpinner, bidButton, challengeButton);
        bidControlBox.setAlignment(Pos.CENTER);
        bidControlBox.setPadding(new Insets(10, 15, 10, 15));
        bidControlBox.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 10;");

        VBox bottomBox = new VBox(8, quickBidBox, bidControlBox);
        bottomBox.setAlignment(Pos.CENTER);
        return bottomBox;
    }

    /**
     * 创建快捷叫牌按钮
     */
    private HBox createQuickBidBox() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(5, 10, 5, 10));

        Label quickLabel = new Label("快捷叫牌:");
        quickLabel.setTextFill(Color.LIGHTGRAY);
        quickLabel.setFont(Font.font("Microsoft YaHei", 12));

        box.getChildren().add(quickLabel);

        for (int face = 1; face <= 6; face++) {
            final int f = face;
            String symbol = switch (face) {
                case 1 -> "⚀";
                case 2 -> "⚁";
                case 3 -> "⚂";
                case 4 -> "⚃";
                case 5 -> "⚄";
                case 6 -> "⚅";
                default -> "?";
            };
            Button btn = new Button(symbol + " +" + face);
            btn.setStyle("-fx-font-size: 12px; -fx-padding: 5 10; -fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 5;");
            btn.setOnAction(e -> {
                faceSpinner.getValueFactory().setValue(f);
                Bid currentBid = engine.getCurrentBid();
                if (currentBid == null) {
                    countSpinner.getValueFactory().setValue(1);
                } else {
                    int needed = currentBid.getCount() + 1;
                    countSpinner.getValueFactory().setValue(needed);
                }
            });
            box.getChildren().add(btn);
        }

        // 快速叫1按钮
        Button onesBtn = new Button("⚀ 叫1");
        onesBtn.setStyle("-fx-font-size: 12px; -fx-padding: 5 10; -fx-background-color: #8e44ad; -fx-text-fill: white; -fx-background-radius: 5; -fx-font-weight: bold;");
        onesBtn.setOnAction(e -> {
            faceSpinner.getValueFactory().setValue(1);
            Bid currentBid = engine.getCurrentBid();
            if (currentBid == null) {
                countSpinner.getValueFactory().setValue(1);
            } else if (currentBid.getFace() == 1) {
                countSpinner.getValueFactory().setValue(currentBid.getCount() + 1);
            } else {
                int needed = (int) Math.ceil(currentBid.getCount() / 2.0);
                countSpinner.getValueFactory().setValue(needed);
            }
        });
        box.getChildren().add(onesBtn);

        return box;
    }

    /**
     * 创建右侧日志区域
     */
    private VBox createLogArea() {
        Label logTitle = new Label("📋 游戏日志");
        logTitle.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        logTitle.setTextFill(Color.GOLD);

        logBox = new VBox(2);
        logBox.setPadding(new Insets(5));

        logScrollPane = new ScrollPane(logBox);
        logScrollPane.setPrefWidth(320);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setStyle("-fx-background: #1a1a2e; -fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 8;");
        logBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            logScrollPane.setVvalue(1.0);
        });

        VBox logArea = new VBox(5, logTitle, logScrollPane);
        logArea.setPadding(new Insets(5));
        logArea.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 10;");
        logArea.setPrefWidth(330);

        return logArea;
    }

    /**
     * 设置引擎回调
     */
    private void setupCallbacks() {
        engine.setOnLogUpdate(this::appendLog);
        engine.setOnPlayersUpdate(players -> Platform.runLater(this::refreshPlayerPanels));
        engine.setOnTurnChanged(this::onTurnChanged);
        engine.setOnRoundEnd(result -> Platform.runLater(() -> onRoundEnd(result)));
        engine.setOnGameEnd(msg -> Platform.runLater(() -> onGameEnd(msg)));
    }

    /**
     * 刷新所有玩家面板
     */
    private void refreshPlayerPanels() {
        playersContainer.getChildren().clear();
        playerPanelMap.clear();

        for (Player player : engine.getPlayers()) {
            VBox panel = createPlayerPanel(player);
            playerPanelMap.put(player.getName(), panel);
            playersContainer.getChildren().add(panel);
        }
    }

    /**
     * 创建单个玩家面板
     */
    private VBox createPlayerPanel(Player player) {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(12));
        panel.setPrefWidth(180);
        panel.setAlignment(Pos.CENTER);

        boolean isCurrentPlayer = player.equals(engine.getCurrentPlayer());
        boolean isHuman = player.isHuman();

        // 面板样式
        String bgColor;
        if (player.isEliminated()) {
            bgColor = "rgba(100,100,100,0.5)";
        } else if (isCurrentPlayer) {
            bgColor = "rgba(46,204,113,0.4)";
        } else if (isHuman) {
            bgColor = "rgba(52,152,219,0.4)";
        } else {
            bgColor = "rgba(0,0,0,0.4)";
        }

        String borderColor = isCurrentPlayer ? "#2ecc71" : (isHuman ? "#3498db" : "#555");
        panel.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 12; -fx-border-color: %s; -fx-border-width: 2; -fx-border-radius: 12;",
                bgColor, borderColor));

        if (isCurrentPlayer) {
            panel.setEffect(new Glow(0.3));
        }

        // 玩家名称
        Label nameLabel = new Label(player.getName());
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        nameLabel.setTextFill(isHuman ? Color.LIGHTCYAN : Color.WHITESMOKE);

        // 当前玩家指示
        if (isCurrentPlayer) {
            Label turnIndicator = new Label("▶ 当前回合");
            turnIndicator.setTextFill(Color.LIMEGREEN);
            turnIndicator.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
            panel.getChildren().add(turnIndicator);
        }

        // 淘汰标记
        if (player.isEliminated()) {
            Label elimLabel = new Label("❌ 已淘汰");
            elimLabel.setTextFill(Color.RED);
            elimLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 12));
            panel.getChildren().add(nameLabel);
            panel.getChildren().add(elimLabel);
            return panel;
        }

        panel.getChildren().add(nameLabel);

        // 骰子显示
        HBox diceBox = new HBox(5);
        diceBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < player.getDices().size(); i++) {
            Dice dice = player.getDices().get(i);
            Label diceLabel = new Label();
            diceLabel.setFont(Font.font(28));

            if (isHuman || showAllDice) {
                diceLabel.setText(dice.getUnicodeFace());
                diceLabel.setTextFill(Color.WHITE);
            } else {
                diceLabel.setText("🎲");
                diceLabel.setTextFill(Color.GRAY);
            }

            StackPane dicePane = new StackPane(diceLabel);
            dicePane.setPrefSize(40, 40);
            dicePane.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-background-radius: 6;");
            diceBox.getChildren().add(dicePane);
        }
        panel.getChildren().add(diceBox);

        // 显示骰子值（人类玩家或已开牌时）
        if (isHuman || showAllDice) {
            HBox valueBox = new HBox(4);
            valueBox.setAlignment(Pos.CENTER);
            List<Integer> values = player.getDiceValues();
            for (int val : values) {
                Label vLabel = new Label(String.valueOf(val));
                vLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
                vLabel.setTextFill(val == 1 ? Color.GOLD : Color.WHITESMOKE);
                vLabel.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-padding: 2 6; -fx-background-radius: 3;");
                valueBox.getChildren().add(vLabel);
            }
            panel.getChildren().add(valueBox);
        }

        // 积分
        Label scoreLabel = new Label("积分: " + player.getScore());
        scoreLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        scoreLabel.setTextFill(player.getScore() >= 0 ? Color.GOLD : Color.TOMATO);

        // 连胜
        if (player.getConsecutiveWins() > 0) {
            Label streakLabel = new Label("🔥 " + player.getConsecutiveWins() + "连胜");
            streakLabel.setTextFill(Color.ORANGE);
            streakLabel.setFont(Font.font("Microsoft YaHei", 12));
            panel.getChildren().addAll(scoreLabel, streakLabel);
        } else {
            panel.getChildren().add(scoreLabel);
        }

        return panel;
    }

    /**
     * 轮到某玩家
     */
    private void onTurnChanged(Player player) {
        Platform.runLater(() -> {
            roundLabel.setText("回合: " + engine.getRoundNumber());
            turnLabel.setText("轮到: " + player.getName());
            turnLabel.setTextFill(player.isHuman() ? Color.LIGHTCYAN : Color.WHITESMOKE);

            Bid bid = engine.getCurrentBid();
            currentBidLabel.setText(bid != null ? "当前叫牌: " + bid.getDisplayText() : "当前叫牌: 无");

            boolean isHumanTurn = player.isHuman();
            bidButton.setDisable(!isHumanTurn);
            challengeButton.setDisable(!isHumanTurn || bid == null);

            // 更新countSpinner的最小值
            if (bid != null) {
                countSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, bid.getCount() + 1));
            } else {
                countSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 1));
            }

            if (isHumanTurn) {
                turnLabel.setText("▶ 轮到你了！请叫牌或开牌");
                turnLabel.setTextFill(Color.LIMEGREEN);
            }

            refreshPlayerPanels();
        });
    }

    /**
     * 回合结束
     */
    private void onRoundEnd(RoundResult result) {
        bidButton.setDisable(true);
        challengeButton.setDisable(true);
        turnLabel.setText("回合结束");
        turnLabel.setTextFill(Color.ORANGE);
        refreshPlayerPanels();
    }

    /**
     * 游戏结束
     */
    private void onGameEnd(String message) {
        bidButton.setDisable(true);
        challengeButton.setDisable(true);

        // 弹出对话框
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("游戏结束");
        alert.setHeaderText(null);
        alert.setContentText(message + "\n\n是否再来一局？");
        alert.getButtonTypes().setAll(
                new ButtonType("再来一局", ButtonBar.ButtonData.YES),
                new ButtonType("返回主页", ButtonBar.ButtonData.NO)
        );

        alert.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonBar.ButtonData.YES) {
                app.startGame(settings);
            } else {
                app.showSetupScene();
            }
        });
    }

    /**
     * 处理叫牌
     */
    private void handleBid() {
        if (!engine.isHumanTurn() || !engine.isRoundActive()) return;

        int count = countSpinner.getValue();
        int face = faceSpinner.getValue();

        boolean success = engine.makeBid(count, face);
        if (!success) {
            showAlert("叫牌失败", "请检查你的叫牌是否比上家更高！");
        }
    }

    /**
     * 处理开牌
     */
    private void handleChallenge() {
        if (!engine.isHumanTurn() || !engine.isRoundActive()) return;
        engine.challenge();
    }

    /**
     * 追加日志
     */
    private void appendLog(String message) {
        Platform.runLater(() -> {
            Text text = new Text(message + "\n");
            text.setFill(Color.WHITESMOKE);
            text.setFont(Font.font("Microsoft YaHei", 13));

            // 特殊消息着色
            if (message.contains("✅") || message.contains("获胜") || message.contains("连胜")) {
                text.setFill(Color.LIGHTGREEN);
            } else if (message.contains("❌") || message.contains("失败") || message.contains("淘汰")) {
                text.setFill(Color.TOMATO);
            } else if (message.contains("📊") || message.contains("-----")) {
                text.setFill(Color.GOLD);
            } else if (message.contains("=====")) {
                text.setFill(Color.CYAN);
                text.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
            } else if (message.contains("【")) {
                text.setFill(Color.ORANGE);
            }

            TextFlow flow = new TextFlow(text);
            logBox.getChildren().add(flow);
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}