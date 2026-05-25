package com.game.dice.model;

import com.game.dice.engine.AIConfig;

/**
 * 游戏设置类
 */
public class GameSettings {

    public enum GameMode {
        TARGET_SCORE("目标分赛"),
        FIXED_ROUNDS("局数赛"),
        ELIMINATION("淘汰赛");

        private final String displayName;

        GameMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private int playerCount = 4;        // 玩家总数(含真人)
    private GameMode gameMode = GameMode.TARGET_SCORE;
    private int targetScore = 20;       // 目标分赛的目标分数
    private int fixedRounds = 10;       // 局数赛的固定局数
    private int eliminationStartScore = 20; // 淘汰赛初始分数
    private String humanPlayerName = "玩家";
    private boolean coachMode = true;     // 教练模式（概率提示）
    private AIConfig.AIMode aiMode = AIConfig.AIMode.RULE_BASED;  // AI模式

    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) {
        if (playerCount < 2 || playerCount > 6) throw new IllegalArgumentException("玩家数量必须在2-6之间");
        this.playerCount = playerCount;
    }

    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }

    public int getTargetScore() { return targetScore; }
    public void setTargetScore(int targetScore) { this.targetScore = targetScore; }

    public int getFixedRounds() { return fixedRounds; }
    public void setFixedRounds(int fixedRounds) { this.fixedRounds = fixedRounds; }

    public int getEliminationStartScore() { return eliminationStartScore; }
    public void setEliminationStartScore(int eliminationStartScore) { this.eliminationStartScore = eliminationStartScore; }

    public String getHumanPlayerName() { return humanPlayerName; }
    public void setHumanPlayerName(String humanPlayerName) { this.humanPlayerName = humanPlayerName; }

    public boolean isCoachMode() { return coachMode; }
    public void setCoachMode(boolean coachMode) { this.coachMode = coachMode; }

    public AIConfig.AIMode getAiMode() { return aiMode; }
    public void setAiMode(AIConfig.AIMode aiMode) { this.aiMode = aiMode; }
}
