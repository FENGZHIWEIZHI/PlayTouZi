package com.game.dice.engine;

import com.game.dice.model.Bid;
import com.game.dice.model.Player;

import java.util.*;

/**
 * 概率计算器 - 实时计算叫牌概率
 */
public class ProbabilityCalculator {

    /**
     * 计算当前叫牌的理论概率
     * @param myDice 我的骰子值
     * @param bid 当前叫牌
     * @param totalDiceCount 总骰子数
     * @return 概率 (0.0 ~ 1.0)
     */
    public static double calculateProbability(List<Integer> myDice, Bid bid, int totalDiceCount) {
        int face = bid.getFace();
        int requiredCount = bid.getCount();

        // 统计自己有多少匹配的骰子
        int myMatchCount = 0;
        for (int val : myDice) {
            if (face == 1) {
                if (val == 1) myMatchCount++;
            } else {
                if (val == face || val == 1) myMatchCount++;
            }
        }

        int remaining = requiredCount - myMatchCount;
        if (remaining <= 0) return 0.95; // 自己就够

        int otherDice = totalDiceCount - myDice.size();

        // 每颗骰子匹配的概率
        double matchProb = (face == 1) ? 1.0 / 6.0 : 2.0 / 6.0;

        // 使用正态近似
        double mean = otherDice * matchProb;
        double variance = otherDice * matchProb * (1 - matchProb);
        double stddev = Math.sqrt(variance);

        if (stddev == 0) return remaining <= mean ? 0.9 : 0.1;

        double z = (remaining - mean) / stddev;
        return 1.0 - normalCDF(z);
    }

    /**
     * 计算如果开牌的胜率
     * @param myDice 我的骰子值
     * @param bid 当前叫牌
     * @param totalDiceCount 总骰子数
     * @return 开牌者获胜概率 (0.0 ~ 1.0)
     */
    public static double calculateChallengeWinProbability(List<Integer> myDice, Bid bid, int totalDiceCount) {
        return 1.0 - calculateProbability(myDice, bid, totalDiceCount);
    }

    /**
     * 生成概率提示文本
     */
    public static String getProbabilityHint(List<Integer> myDice, Bid bid, int totalDiceCount) {
        double prob = calculateProbability(myDice, bid, totalDiceCount);
        int percent = (int) (prob * 100);

        String level;
        if (percent >= 80) level = "🟢 高概率";
        else if (percent >= 50) level = "🟡 中等概率";
        else if (percent >= 25) level = "🟠 较低概率";
        else level = "🔴 低概率";

        return String.format("%s (%d%%)", level, percent);
    }

    /**
     * 生成建议文本
     */
    public static String getSuggestion(List<Integer> myDice, Bid currentBid, int totalDiceCount) {
        if (currentBid == null) return "请开始叫牌";

        double bidProb = calculateProbability(myDice, currentBid, totalDiceCount);
        double challengeWinProb = 1.0 - bidProb;

        if (challengeWinProb > 0.65) {
            return "💡 建议开牌（对方叫牌偏低，开牌胜率 " + (int)(challengeWinProb * 100) + "%）";
        } else if (bidProb > 0.7) {
            return "💡 建议继续叫牌（当前叫牌概率较高 " + (int)(bidProb * 100) + "%）";
        } else {
            return "💡 局势不明朗，谨慎决策";
        }
    }

    /**
     * 计算指定叫牌的实际数量（开牌后）
     */
    public static int countActual(List<Player> players, Bid bid) {
        int count = 0;
        int face = bid.getFace();

        for (Player p : players) {
            if (p.isEliminated()) continue;
            for (int val : p.getDiceValues()) {
                if (face == 1) {
                    if (val == 1) count++;
                } else {
                    if (val == face || val == 1) count++;
                }
            }
        }
        return count;
    }

    private static double normalCDF(double z) {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    private static double erf(double x) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));
        double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t
                - 0.284496736) * t + 0.254829592) * t * Math.exp(-x * x);
        return x >= 0 ? y : -y;
    }
}