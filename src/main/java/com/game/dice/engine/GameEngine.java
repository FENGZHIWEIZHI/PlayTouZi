package com.game.dice.engine;

import com.game.dice.model.*;
import javafx.application.Platform;

import java.util.*;
import java.util.function.Consumer;

/**
 * 游戏引擎 - 控制整个游戏流程
 */
public class GameEngine {
    private final GameSettings settings;
    private final List<Player> players;
    private final List<String> gameLog;
    private int currentPlayerIndex;
    private int roundNumber;
    private Bid currentBid;
    private Player lastBidder;
    private boolean roundActive;
    private boolean gameActive;

    // 新增系统
    private GameStats gameStats;
    private Achievement achievement;
    private SkinManager skinManager;

    // 回调
    private Consumer<String> onLogUpdate;
    private Consumer<List<Player>> onPlayersUpdate;
    private Consumer<Player> onTurnChanged;
    private Consumer<RoundResult> onRoundEnd;
    private Consumer<String> onGameEnd;
    private Consumer<List<String>> onAchievementUnlocked;
    private Runnable onGameStateChanged;

    public GameEngine(GameSettings settings) {
        this.settings = settings;
        this.players = new ArrayList<>();
        this.gameLog = new ArrayList<>();
        this.roundNumber = 0;
        this.gameActive = false;
        this.achievement = new Achievement();
        this.skinManager = new SkinManager();

        // 创建玩家
        players.add(new Player(settings.getHumanPlayerName(), true));
        String[] botNames = {"机器人A", "机器人B", "机器人C", "机器人D", "机器人E"};
        for (int i = 1; i < settings.getPlayerCount(); i++) {
            players.add(new Player(botNames[i - 1], false));
        }
    }

    // ==================== 回调设置 ====================

    public void setOnLogUpdate(Consumer<String> callback) { this.onLogUpdate = callback; }
    public void setOnPlayersUpdate(Consumer<List<Player>> callback) { this.onPlayersUpdate = callback; }
    public void setOnTurnChanged(Consumer<Player> callback) { this.onTurnChanged = callback; }
    public void setOnAchievementUnlocked(Consumer<List<String>> callback) { this.onAchievementUnlocked = callback; }
    public void setOnRoundEnd(Consumer<RoundResult> callback) { this.onRoundEnd = callback; }
    public void setOnGameEnd(Consumer<String> callback) { this.onGameEnd = callback; }
    public void setOnGameStateChanged(Runnable callback) { this.onGameStateChanged = callback; }

    // ==================== 游戏控制 ====================

    /**
     * 开始游戏
     */
    public void startGame() {
        gameActive = true;
        gameStats = new GameStats(players);
        addLog("===== 游戏开始 =====");
        addLog("模式: " + settings.getGameMode().getDisplayName());
        addLog("玩家数: " + settings.getPlayerCount());
        switch (settings.getGameMode()) {
            case TARGET_SCORE -> addLog("目标分数: " + settings.getTargetScore());
            case FIXED_ROUNDS -> addLog("固定局数: " + settings.getFixedRounds());
            case ELIMINATION -> addLog("初始分数: " + settings.getEliminationStartScore());
        }
        addLog("========================");
        startNewRound();
    }

    /**
     * 开始新回合
     */
    private void startNewRound() {
        roundNumber++;
        currentBid = null;
        lastBidder = null;
        roundActive = true;

        // 重新掷骰子
        for (Player p : players) {
            if (!p.isEliminated()) {
                p.rollAllDices();
            }
        }

        addLog("\n----- 第 " + roundNumber + " 回合 -----");
        updatePlayers();

        // 确定起始玩家（第一回合随机，之后轮换）
        if (roundNumber == 1) {
            currentPlayerIndex = new Random().nextInt(players.size());
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            // 跳过被淘汰的玩家
            while (players.get(currentPlayerIndex).isEliminated()) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            }
        }

        addLog(players.get(currentPlayerIndex).getName() + " 先手");
        notifyTurn();
    }

    /**
     * 处理叫牌
     */
    public boolean makeBid(int count, int face) {
        if (!roundActive || !gameActive) return false;

        Player current = players.get(currentPlayerIndex);

        Bid newBid = new Bid(count, face);

        // 检查叫牌是否合法
        if (!newBid.isValidAgainst(currentBid)) {
            addLog("❌ 非法叫牌！必须比 " + currentBid.getDisplayText() + " 更高");
            return false;
        }

        currentBid = newBid;
        lastBidder = current;
        addLog(current.getName() + " 叫: " + currentBid.getDisplayText());

        // 检查10连胜直接获胜
        if (current.getConsecutiveWins() >= 9) {
            // 叫牌后如果赢了就是10连胜
        }

        nextTurn();
        return true;
    }

    /**
     * 处理开牌（睇/开）
     */
    public void challenge() {
        if (!roundActive || !gameActive) return;
        if (currentBid == null) {
            // 无人叫牌时抢开
            Player current = players.get(currentPlayerIndex);
            String msg = ScoreManager.applyFalseOpenPenalty(current, true);
            addLog(msg);
            updatePlayers();
            checkGameEnd();
            if (gameActive) startNewRound();
            return;
        }

        Player challenger = players.get(currentPlayerIndex);
        Player caller = lastBidder;

        addLog(challenger.getName() + " 开牌！质疑 " + caller.getName() + " 的 " + currentBid.getDisplayText());

        // 统计所有骰子
        Map<Player, List<Integer>> allDiceValues = new LinkedHashMap<>();
        int totalCount = 0;
        for (Player p : players) {
            if (!p.isEliminated()) {
                List<Integer> values = p.getDiceValues();
                allDiceValues.put(p, values);
                for (int val : values) {
                    if (val == currentBid.getFace() || (currentBid.getFace() != 1 && val == 1)) {
                        totalCount++;
                    }
                }
            }
        }

        // 叫1的情况：只统计1的数量
        if (currentBid.getFace() == 1) {
            totalCount = 0;
            for (Player p : players) {
                if (!p.isEliminated()) {
                    totalCount += p.countOnes();
                }
            }
        }

        boolean callerWins = totalCount >= currentBid.getCount();

        // 构建奖励消息
        StringBuilder bonusMsg = new StringBuilder();

        // 计算并应用积分
        RoundResult result = new RoundResult(caller, challenger, currentBid, totalCount,
                callerWins, allDiceValues, "");
        List<String> scoreMessages = ScoreManager.applyRoundResult(result, settings);

        // 显示结果
        addLog("\n📊 开牌结果:");
        for (Player p : players) {
            if (!p.isEliminated()) {
                List<Integer> values = allDiceValues.get(p);
                addLog("  " + p.getName() + ": " + diceToString(values));
            }
        }
        addLog("  " + currentBid.getFace() + "点 总数: " + totalCount + " (叫了 " + currentBid.getCount() + ")");

        if (callerWins) {
            addLog("  ✅ " + caller.getName() + " 叫牌正确！" + challenger.getName() + " 输了");
        } else {
            addLog("  ❌ " + caller.getName() + " 叫牌过高！" + caller.getName() + " 输了");
        }

        for (String msg : scoreMessages) {
            addLog("  " + msg);
        }

        roundActive = false;

        // 记录统计
        if (gameStats != null) {
            gameStats.recordRound(result);
            // 更新成就
            GameStats.PlayerStats humanStats = gameStats.getPlayerStats(getHumanPlayer().getName());
            if (humanStats != null) {
                List<String> achNotifications = achievement.updateProgress(humanStats, getHumanPlayer().getConsecutiveWins());
                if (!achNotifications.isEmpty() && onAchievementUnlocked != null) {
                    onAchievementUnlocked.accept(achNotifications);
                }
                // 解锁对应皮肤
                for (Achievement.AchievementType ach : achievement.getUnlocked()) {
                    SkinManager.SkinType skin = SkinManager.getSkinForAchievement(ach);
                    if (skin != null) skinManager.unlockSkin(skin);
                }
            }
        }

        updatePlayers();

        if (onRoundEnd != null) {
            onRoundEnd.accept(result);
        }

        checkGameEnd();

        if (gameActive) {
            // 延迟后开始新回合
            new Thread(() -> {
                try { Thread.sleep(3000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                Platform.runLater(this::startNewRound);
            }).start();
        }
    }

    /**
     * 处理机器人的回合
     */
    public void handleBotTurn() {
        if (!roundActive || !gameActive) return;

        Player current = players.get(currentPlayerIndex);
        if (current.isHuman() || current.isEliminated()) return;

        // LLM模式需要更长的思考时间（网络请求）
        boolean useLLM = isLLMAvailable();
        int minDelay = useLLM ? 500 : 1500;
        int maxDelay = useLLM ? 1500 : 1500;

        // 延迟模拟思考
        new Thread(() -> {
            try { Thread.sleep(minDelay + new Random().nextInt(maxDelay)); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }

            Platform.runLater(() -> {
                if (!roundActive || !gameActive) return;

                BotDecision decision;
                if (useLLM) {
                    decision = LLMBotAI.makeDecision(current, currentBid, lastBidder, players, roundNumber);
                } else {
                    decision = BotAI.makeDecision(current, currentBid, lastBidder, players, roundNumber);
                }

                if (decision.isChallenge()) {
                    challenge();
                } else {
                    Bid botBid = decision.getBid();
                    makeBid(botBid.getCount(), botBid.getFace());
                }
            });
        }).start();
    }

    /**
     * 判断是否应该使用LLM AI
     */
    private boolean isLLMAvailable() {
        AIConfig.AIMode mode = settings.getAiMode();
        AIConfig config = AIConfig.getInstance();
        if (mode == AIConfig.AIMode.LLM || mode == AIConfig.AIMode.HYBRID) {
            return config.isApiKeyConfigured();
        }
        return false;
    }

    /**
     * 轮到下一个玩家
     */
    private void nextTurn() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (players.get(currentPlayerIndex).isEliminated());

        notifyTurn();
    }

    /**
     * 通知当前轮到谁
     */
    private void notifyTurn() {
        Player current = players.get(currentPlayerIndex);
        if (onTurnChanged != null) {
            onTurnChanged.accept(current);
        }
        // 如果是机器人，自动处理
        if (!current.isHuman() && roundActive) {
            handleBotTurn();
        }
    }

    /**
     * 检查游戏是否结束
     */
    private void checkGameEnd() {
        String endMsg = null;

        switch (settings.getGameMode()) {
            case TARGET_SCORE -> {
                for (Player p : players) {
                    if (p.getScore() >= settings.getTargetScore()) {
                        endMsg = "🏆 " + p.getName() + " 达到 " + settings.getTargetScore() + " 分，获得胜利！";
                        break;
                    }
                }
            }
            case FIXED_ROUNDS -> {
                if (roundNumber >= settings.getFixedRounds()) {
                    Player winner = players.stream()
                            .filter(p -> !p.isEliminated())
                            .max(Comparator.comparingInt(Player::getScore))
                            .orElse(null);
                    if (winner != null) {
                        endMsg = "🏆 " + winner.getName() + " 以 " + winner.getScore() + " 分获得胜利！";
                    }
                }
            }
            case ELIMINATION -> {
                List<Player> alive = players.stream()
                        .filter(p -> !p.isEliminated()).toList();
                if (alive.size() <= 1) {
                    if (alive.size() == 1) {
                        endMsg = "🏆 " + alive.get(0).getName() + " 是最后的幸存者，获得胜利！";
                    } else {
                        endMsg = "所有玩家均被淘汰，平局！";
                    }
                }
            }
        }

        // 10连胜直接获胜
        for (Player p : players) {
            if (p.getConsecutiveWins() >= 10) {
                endMsg = "🏆 " + p.getName() + " 达成10连胜，直接获得胜利！";
                break;
            }
        }

        if (endMsg != null) {
            gameActive = false;
            roundActive = false;
            addLog("\n===== 游戏结束 =====");
            addLog(endMsg);
            // 显示最终积分
            addLog("\n最终积分:");
            players.stream()
                    .sorted(Comparator.comparingInt(Player::getScore).reversed())
                    .forEach(p -> addLog("  " + p.getName() + ": " + p.getScore() + " 分"));
            if (onGameEnd != null) {
                onGameEnd.accept(endMsg);
            }
        }
    }

    // ==================== 工具方法 ====================

    private String diceToString(List<Integer> values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(values.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    private void addLog(String message) {
        gameLog.add(message);
        if (onLogUpdate != null) {
            onLogUpdate.accept(message);
        }
    }

    private void updatePlayers() {
        if (onPlayersUpdate != null) {
            onPlayersUpdate.accept(players);
        }
    }

    // ==================== Getters ====================

    public List<Player> getPlayers() { return players; }
    public Bid getCurrentBid() { return currentBid; }
    public GameStats getGameStats() { return gameStats; }
    public Achievement getAchievement() { return achievement; }
    public SkinManager getSkinManager() { return skinManager; }
    public Player getCurrentPlayer() {
        if (currentPlayerIndex >= 0 && currentPlayerIndex < players.size()) {
            return players.get(currentPlayerIndex);
        }
        return null;
    }
    public Player getLastBidder() { return lastBidder; }
    public int getRoundNumber() { return roundNumber; }
    public boolean isRoundActive() { return roundActive; }
    public boolean isGameActive() { return gameActive; }
    public GameSettings getSettings() { return settings; }
    public List<String> getGameLog() { return gameLog; }

    /**
     * 获取人类玩家
     */
    public Player getHumanPlayer() {
        return players.stream().filter(Player::isHuman).findFirst().orElse(null);
    }

    /**
     * 判断当前是否轮到人类玩家
     */
    public boolean isHumanTurn() {
        Player current = getCurrentPlayer();
        return current != null && current.isHuman();
    }
}