package com.game.dice.model;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 玩家档案 - 跨局持久化统计数据和成就
 */
public class PlayerProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String SAVE_FILE = System.getProperty("user.home") + "/.liars_dice_profile.dat";

    private String playerName = "玩家";

    // 全局累计统计
    private int totalGames = 0;         // 总对局数
    private int totalWins = 0;          // 总胜利数
    private int totalRounds = 0;        // 总回合数
    private int totalCalls = 0;         // 总叫牌次数
    private int totalCallFails = 0;     // 叫牌失败次数
    private int totalChallenges = 0;    // 总开牌次数
    private int totalSuccessfulChallenges = 0; // 成功开牌次数
    private int totalPreciseCalls = 0;  // 精准叫牌次数
    private int totalBluffs = 0;        // 总诈唬次数
    private int totalSuccessfulBluffs = 0; // 成功诈唬次数
    private int totalFailedBluffs = 0;  // 失败诈唬次数
    private int totalBluffsCaught = 0;  // 识破诈唬次数
    private int totalOnesTactics = 0;   // 1点战术次数
    private int totalTimeouts = 0;      // 总超时次数
    private int maxConsecutiveNoTimeout = 0; // 最大连续不超时回合数
    private int maxWinStreak = 0;       // 最大连胜记录
    private int bestScore = 0;          // 单局最高分

    // 成就进度
    private Map<String, Integer> achievementProgress = new LinkedHashMap<>();
    private Set<String> unlockedAchievements = new LinkedHashSet<>();

    // 解锁的皮肤
    private Set<String> unlockedSkins = new LinkedHashSet<>();
    private String selectedTableSkin = "CLASSIC_GREEN";
    private String selectedCupSkin = "WOODEN_CUP";

    // ==================== 更新统计 ====================

    public void recordGame(boolean won, int score) {
        totalGames++;
        if (won) totalWins++;
        if (score > bestScore) bestScore = score;
        save();
    }

    public void recordRound(GameStats.PlayerStats stats) {
        totalRounds++;
        totalCalls += stats.totalCalls;
        totalCallFails += stats.totalCallFails;
        totalChallenges += stats.totalChallenges;
        totalSuccessfulChallenges += stats.successfulChallenges;
        totalPreciseCalls += stats.preciseCalls;
        totalBluffs += stats.totalBluffs;
        totalSuccessfulBluffs += stats.successfulBluffs;
        totalFailedBluffs += stats.failedBluffs;
        totalBluffsCaught += stats.bluffsCaught;
        totalOnesTactics += stats.onesTactics;

        // 更新成就
        updateAchievementProgress("PRECISION_MACHINE", totalPreciseCalls);
        updateAchievementProgress("BLUFF_HUNTER", totalBluffsCaught);
        updateAchievementProgress("ONES_MASTER", totalOnesTactics);
        updateAchievementProgress("VETERAN", totalRounds);
        updateAchievementProgress("CHALLENGE_MASTER", totalSuccessfulChallenges);

        save();
    }

    public void recordWinStreak(int streak) {
        if (streak > maxWinStreak) maxWinStreak = streak;
        updateAchievementProgress("WIN_STREAK_3", streak);
        updateAchievementProgress("WIN_STREAK_5", streak);
        updateAchievementProgress("WIN_STREAK_7", streak);
        save();
    }

    public void recordBluffSuccess(int currentGameBluffs) {
        updateAchievementProgress("PSYCH_MASTER", currentGameBluffs);
        save();
    }

    public void recordNoTimeout(int consecutive) {
        if (consecutive > maxConsecutiveNoTimeout) maxConsecutiveNoTimeout = consecutive;
        updateAchievementProgress("TIME_MANAGER", consecutive);
        save();
    }

    private void updateAchievementProgress(String key, int value) {
        achievementProgress.put(key, value);
        Achievement.AchievementType type = Achievement.AchievementType.valueOf(key);
        if (value >= type.getTarget()) {
            if (unlockedAchievements.add(key)) {
                // 解锁对应皮肤
                SkinManager.SkinType skin = SkinManager.getSkinForAchievement(type);
                if (skin != null) unlockedSkins.add(skin.name());
            }
        }
    }

    // ==================== 成就查询 ====================

    public boolean isAchievementUnlocked(Achievement.AchievementType type) {
        return unlockedAchievements.contains(type.name());
    }

    public int getAchievementProgress(Achievement.AchievementType type) {
        return achievementProgress.getOrDefault(type.name(), 0);
    }

    // ==================== 皮肤 ====================

    public boolean isSkinUnlocked(SkinManager.SkinType skin) {
        return unlockedSkins.contains(skin.name());
    }

    // ==================== Getters ====================

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public int getTotalGames() { return totalGames; }
    public int getTotalWins() { return totalWins; }
    public int getTotalRounds() { return totalRounds; }
    public int getTotalCalls() { return totalCalls; }
    public int getTotalCallFails() { return totalCallFails; }
    public int getTotalChallenges() { return totalChallenges; }
    public int getTotalSuccessfulChallenges() { return totalSuccessfulChallenges; }
    public int getTotalPreciseCalls() { return totalPreciseCalls; }
    public int getTotalBluffs() { return totalBluffs; }
    public int getTotalSuccessfulBluffs() { return totalSuccessfulBluffs; }
    public int getTotalFailedBluffs() { return totalFailedBluffs; }
    public int getTotalBluffsCaught() { return totalBluffsCaught; }
    public int getTotalOnesTactics() { return totalOnesTactics; }
    public int getTotalTimeouts() { return totalTimeouts; }
    public int getMaxWinStreak() { return maxWinStreak; }
    public int getBestScore() { return bestScore; }
    public Set<String> getUnlockedAchievements() { return unlockedAchievements; }
    public Set<String> getUnlockedSkins() { return unlockedSkins; }
    public String getSelectedTableSkin() { return selectedTableSkin; }
    public void setSelectedTableSkin(String s) { this.selectedTableSkin = s; save(); }
    public String getSelectedCupSkin() { return selectedCupSkin; }
    public void setSelectedCupSkin(String s) { this.selectedCupSkin = s; save(); }

    public String getWinRate() {
        if (totalGames == 0) return "N/A";
        return String.format("%.0f%%", totalWins * 100.0 / totalGames);
    }

    public String getCallSuccessRate() {
        if (totalCalls == 0) return "N/A";
        return String.format("%.0f%%", (totalCalls - totalCallFails) * 100.0 / totalCalls);
    }

    public String getChallengeSuccessRate() {
        if (totalChallenges == 0) return "N/A";
        return String.format("%.0f%%", totalSuccessfulChallenges * 100.0 / totalChallenges);
    }

    public String getBluffRate() {
        if (totalBluffs == 0) return "N/A";
        return String.format("%.0f%%", totalSuccessfulBluffs * 100.0 / totalBluffs);
    }

    // ==================== 持久化 ====================

    public void save() {
        try {
            Files.createDirectories(Paths.get(SAVE_FILE).getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
                oos.writeObject(this);
            }
        } catch (IOException e) {
            System.err.println("保存玩家档案失败: " + e.getMessage());
        }
    }

    public static PlayerProfile load() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return new PlayerProfile();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (PlayerProfile) ois.readObject();
        } catch (Exception e) {
            System.err.println("加载玩家档案失败: " + e.getMessage());
            return new PlayerProfile();
        }
    }
}