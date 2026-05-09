package com.game.dice.model;

import java.util.*;

/**
 * 游戏统计数据 - 跟踪所有关键指标
 */
public class GameStats {
    private final Map<String, PlayerStats> playerStatsMap = new LinkedHashMap<>();
    private final List<RoundHistory> roundHistory = new ArrayList<>();

    public GameStats(List<Player> players) {
        for (Player p : players) {
            playerStatsMap.put(p.getName(), new PlayerStats(p.getName(), p.isHuman()));
        }
    }

    /**
     * 记录一局结果
     */
    public void recordRound(RoundResult result) {
        RoundHistory history = new RoundHistory(
            result.getCaller().getName(),
            result.getChallenger().getName(),
            result.getLastBid(),
            result.getActualCount(),
            result.isCallerWins(),
            result.getAllDiceValues()
        );
        roundHistory.add(history);

        // 更新叫牌者统计
        PlayerStats callerStats = playerStatsMap.get(result.getCaller().getName());
        if (callerStats != null) {
            callerStats.totalCalls++;
            if (result.isCallerWins()) {
                // 叫牌者赢了
            } else {
                callerStats.totalCallFails++;
            }
            // 精准叫牌
            if (result.getLastBid().getCount() == result.getActualCount()) {
                callerStats.preciseCalls++;
            }
            // 诈唬检测
            if (result.getLastBid().getCount() >= result.getActualCount() + 3 && !result.isCallerWins()) {
                callerStats.totalBluffs++;
                callerStats.failedBluffs++;
            }
            // 1点战术
            if (result.getLastBid().getFace() == 1) {
                callerStats.onesTactics++;
            }
        }

        // 更新开牌者统计
        PlayerStats challengerStats = playerStatsMap.get(result.getChallenger().getName());
        if (challengerStats != null) {
            challengerStats.totalChallenges++;
            if (!result.isCallerWins()) {
                challengerStats.successfulChallenges++;
                // 成功识破诈唬
                if (result.getLastBid().getCount() >= result.getActualCount() + 3) {
                    challengerStats.bluffsCaught++;
                }
            }
        }
    }

    /**
     * 记录成功的诈唬（叫牌者叫高但没人开）
     */
    public void recordSuccessfulBluff(String playerName) {
        PlayerStats stats = playerStatsMap.get(playerName);
        if (stats != null) {
            stats.totalBluffs++;
            stats.successfulBluffs++;
        }
    }

    /**
     * 记录超时
     */
    public void recordTimeout(String playerName) {
        PlayerStats stats = playerStatsMap.get(playerName);
        if (stats != null) {
            stats.timeouts++;
            stats.consecutiveNoTimeout = 0;
        }
    }

    /**
     * 记录无超时回合
     */
    public void recordNoTimeout(String playerName) {
        PlayerStats stats = playerStatsMap.get(playerName);
        if (stats != null) {
            stats.consecutiveNoTimeout++;
        }
    }

    public PlayerStats getPlayerStats(String name) {
        return playerStatsMap.get(name);
    }

    public Map<String, PlayerStats> getAllStats() {
        return playerStatsMap;
    }

    public List<RoundHistory> getRoundHistory() {
        return roundHistory;
    }

    /**
     * 玩家统计数据
     */
    public static class PlayerStats {
        public final String playerName;
        public final boolean isHuman;
        public int totalCalls = 0;        // 总叫牌次数
        public int totalCallFails = 0;    // 叫牌失败次数
        public int totalChallenges = 0;   // 总开牌次数
        public int successfulChallenges = 0; // 成功开牌次数
        public int preciseCalls = 0;      // 精准叫牌次数
        public int totalBluffs = 0;       // 总诈唬次数
        public int successfulBluffs = 0;  // 成功诈唬次数
        public int failedBluffs = 0;      // 失败诈唬次数
        public int bluffsCaught = 0;      // 识破诈唬次数
        public int onesTactics = 0;       // 1点战术使用次数
        public int timeouts = 0;          // 超时次数
        public int consecutiveNoTimeout = 0; // 连续不超时回合数

        public PlayerStats(String playerName, boolean isHuman) {
            this.playerName = playerName;
            this.isHuman = isHuman;
        }

        public String getBluffRate() {
            if (totalBluffs == 0) return "N/A";
            return String.format("%.0f%%", (successfulBluffs * 100.0 / totalBluffs));
        }

        public String getChallengeSuccessRate() {
            if (totalChallenges == 0) return "N/A";
            return String.format("%.0f%%", (successfulChallenges * 100.0 / totalChallenges));
        }

        public String getCallSuccessRate() {
            if (totalCalls == 0) return "N/A";
            return String.format("%.0f%%", ((totalCalls - totalCallFails) * 100.0 / totalCalls));
        }
    }

    /**
     * 单局历史记录
     */
    public static class RoundHistory {
        public final String callerName;
        public final String challengerName;
        public final Bid bid;
        public final int actualCount;
        public final boolean callerWins;
        public final Map<Player, List<Integer>> allDiceValues;

        public RoundHistory(String callerName, String challengerName, Bid bid,
                           int actualCount, boolean callerWins,
                           Map<Player, List<Integer>> allDiceValues) {
            this.callerName = callerName;
            this.challengerName = challengerName;
            this.bid = bid;
            this.actualCount = actualCount;
            this.callerWins = callerWins;
            this.allDiceValues = allDiceValues;
        }

        /**
         * 计算如果当时开牌的胜率
         */
        public String getWhatIfAnalysis() {
            if (callerWins) {
                int over = bid.getCount() - actualCount;
                if (over == 0) {
                    return "🎯 精准叫牌！数量完全一致";
                } else {
                    return "叫牌者有 " + (actualCount * 100 / bid.getCount()) + "% 的底气";
                }
            } else {
                return "开牌正确！实际只有 " + actualCount + "个" + bid.getFace() + "点";
            }
        }
    }
}