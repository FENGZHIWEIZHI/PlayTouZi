package com.game.dice.ui;

import com.game.dice.MainApp;
import com.game.dice.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * 游戏设置界面 - 包含玩家档案、成就、统计、皮肤
 */
public class GameSetupScene {

    private final MainApp app;
    private final GameSettings settings;
    private final PlayerProfile profile;

    public GameSetupScene(MainApp app, PlayerProfile profile) {
        this.app = app;
        this.settings = new GameSettings();
        this.profile = profile;
        // 从档案恢复玩家名
        settings.setHumanPlayerName(profile.getPlayerName());
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

        // 玩家档案摘要
        VBox profileBox = createProfileSummary();

        // 设置面板
        VBox settingsBox = new VBox(12);
        settingsBox.setPadding(new Insets(20));
        settingsBox.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-background-radius: 15;");
        settingsBox.setMaxWidth(450);

        // 玩家名称
        Label nameLabel = createLabel("玩家名称:");
        TextField nameField = new TextField(profile.getPlayerName());
        nameField.setPromptText("输入你的名字");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");
        nameField.textProperty().addListener((obs, old, val) -> {
            settings.setHumanPlayerName(val);
            profile.setPlayerName(val);
        });

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

        // 教练模式
        CheckBox coachCheckBox = new CheckBox("开启教练模式（概率提示）");
        coachCheckBox.setSelected(true);
        coachCheckBox.setTextFill(Color.WHITESMOKE);
        coachCheckBox.setFont(Font.font("Microsoft YaHei", 14));
        coachCheckBox.selectedProperty().addListener((obs, old, val) -> settings.setCoachMode(val));

        // 功能按钮区
        HBox featureBox = createFeatureButtons();

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
                coachCheckBox
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
        VBox mainBox = new VBox(15, titleBox, profileBox, settingsBox, featureBox, btnBox, rulesBox);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(20));
        mainBox.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e, #0f3460);");
        VBox.setVgrow(mainBox, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(mainBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        StackPane root = new StackPane(scrollPane);
        root.setStyle("-fx-background-color: #1a1a2e;");

        Scene scene = new Scene(root);
        return scene;
    }

    /**
     * 玩家档案摘要
     */
    private VBox createProfileSummary() {
        Label title = new Label("📊 " + profile.getPlayerName() + " 的档案");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 18));
        title.setTextFill(Color.GOLD);

        Label gameInfo = new Label(String.format("总对局: %d | 胜率: %s | 最高分: %d | 最大连胜: %d",
                profile.getTotalGames(), profile.getWinRate(), profile.getBestScore(), profile.getMaxWinStreak()));
        gameInfo.setFont(Font.font("Microsoft YaHei", 14));
        gameInfo.setTextFill(Color.WHITESMOKE);

        Label roundInfo = new Label(String.format("总回合: %d | 精准叫牌: %d | 诈唬: %d次 (成功率: %s)",
                profile.getTotalRounds(), profile.getTotalPreciseCalls(), profile.getTotalBluffs(), profile.getBluffRate()));
        roundInfo.setFont(Font.font("Microsoft YaHei", 14));
        roundInfo.setTextFill(Color.LIGHTGRAY);

        Label achieveInfo = new Label(String.format("🏆 已解锁成就: %d/%d | 🎨 已解锁皮肤: %d",
                profile.getUnlockedAchievements().size(), Achievement.AchievementType.values().length,
                profile.getUnlockedSkins().size()));
        achieveInfo.setFont(Font.font("Microsoft YaHei", 14));
        achieveInfo.setTextFill(Color.ORANGE);

        VBox box = new VBox(6, title, gameInfo, roundInfo, achieveInfo);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 12;");
        box.setMaxWidth(550);
        return box;
    }

    /**
     * 功能按钮区
     */
    private HBox createFeatureButtons() {
        Button statsBtn = new Button("📈 详细统计");
        statsBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        statsBtn.setOnAction(e -> showStatsDialog());

        Button achieveBtn = new Button("🏆 成就系统");
        achieveBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        achieveBtn.setOnAction(e -> showAchievementDialog());

        Button skinBtn = new Button("🎨 皮肤中心");
        skinBtn.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
        skinBtn.setOnAction(e -> showSkinDialog());

        HBox box = new HBox(15, statsBtn, achieveBtn, skinBtn);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    /**
     * 详细统计对话框
     */
    private void showStatsDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("📈 详细游戏统计\n\n");
        sb.append(String.format("总对局数: %d\n", profile.getTotalGames()));
        sb.append(String.format("总胜利数: %d (胜率: %s)\n", profile.getTotalWins(), profile.getWinRate()));
        sb.append(String.format("最高分: %d  最大连胜: %d\n\n", profile.getBestScore(), profile.getMaxWinStreak()));
        sb.append(String.format("总回合数: %d\n", profile.getTotalRounds()));
        sb.append(String.format("叫牌次数: %d (成功率: %s)\n", profile.getTotalCalls(), profile.getCallSuccessRate()));
        sb.append(String.format("开牌次数: %d (成功率: %s)\n\n", profile.getTotalChallenges(), profile.getChallengeSuccessRate()));
        sb.append(String.format("精准叫牌: %d次\n", profile.getTotalPreciseCalls()));
        sb.append(String.format("诈唬次数: %d (成功率: %s)\n", profile.getTotalBluffs(), profile.getBluffRate()));
        sb.append(String.format("识破诈唬: %d次\n", profile.getTotalBluffsCaught()));
        sb.append(String.format("1点战术: %d次\n", profile.getTotalOnesTactics()));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("详细统计");
        alert.setHeaderText(profile.getPlayerName() + " 的游戏数据");
        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }

    /**
     * 成就系统对话框
     */
    private void showAchievementDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("🏆 成就系统\n\n");
        for (Achievement.AchievementType type : Achievement.AchievementType.values()) {
            int progress = profile.getAchievementProgress(type);
            boolean unlocked = profile.isAchievementUnlocked(type);
            String status = unlocked ? "✅ 已解锁" : String.format("⬜ %d/%d", progress, type.getTarget());
            sb.append(String.format("%s %s %s\n   %s\n\n", type.getIcon(), type.getDisplayName(), status, type.getDescription()));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("成就系统");
        alert.setHeaderText("已解锁 " + profile.getUnlockedAchievements().size() + "/" + Achievement.AchievementType.values().length + " 个成就");
        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(550);
        alert.showAndWait();
    }

    /**
     * 皮肤中心对话框
     */
    private void showSkinDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append("🎨 皮肤中心\n\n");

        sb.append("【台面皮肤】\n");
        for (SkinManager.SkinType skin : SkinManager.SkinType.values()) {
            if (skin.getCategory() != SkinManager.SkinCategory.TABLE) continue;
            boolean unlocked = profile.isSkinUnlocked(skin);
            String status = unlocked ? "✅" : "🔒";
            sb.append(String.format("  %s %s %s - %s\n", skin.getIcon(), skin.getDisplayName(), status, skin.getDescription()));
        }

        sb.append("\n【骰盅皮肤】\n");
        for (SkinManager.SkinType skin : SkinManager.SkinType.values()) {
            if (skin.getCategory() != SkinManager.SkinCategory.CUP) continue;
            boolean unlocked = profile.isSkinUnlocked(skin);
            String status = unlocked ? "✅" : "🔒";
            sb.append(String.format("  %s %s %s - %s\n", skin.getIcon(), skin.getDisplayName(), status, skin.getDescription()));
        }

        sb.append("\n通过解锁对应成就来获得皮肤！");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("皮肤中心");
        alert.setHeaderText("已解锁 " + profile.getUnlockedSkins().size() + " 个皮肤");
        alert.setContentText(sb.toString());
        alert.getDialogPane().setPrefWidth(550);
        alert.showAndWait();
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 15));
        label.setTextFill(Color.WHITESMOKE);
        return label;
    }
}