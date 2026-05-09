package com.game.dice.engine;

import com.game.dice.model.Bid;
import com.game.dice.model.Player;

import java.util.*;

/**
 * 机器人AI策略
 * 根据自己的骰子和当前叫牌情况做出决策
 */
public class BotAI {

    private static final Random RANDOM = new Random();

    /**
     * 为机器人做出决策
     */
    public static BotDecision makeDecision(Player bot, Bid currentBid, Player lastBidder,
                                           List<Player> allPlayers, int roundNumber) {
        List<Integer> myDice = bot.getDiceValues();
        int totalDice = allPlayers.stream()
                .filter(p -> !p.isEliminated())
                .mapToInt(p -> p.getDices().size())
                .sum();

        // 如果没有叫牌，第一个叫
        if (currentBid == null) {
            return BotDecision.bid(makeOpeningBid(myDice, totalDice));
        }

        // 计算当前叫牌的实际概率
        double probability = estimateProbability(myDice, currentBid, totalDice);

        // 根据概率决定是否开牌
        double challengeThreshold = getChallengeThreshold(roundNumber, currentBid, lastBidder, bot);

        if (probability < challengeThreshold) {
            return BotDecision.challenge();
        }

        // 否则继续往上叫
        Bid nextBid = calculateNextBid(myDice, currentBid, totalDice, probability);
        if (nextBid != null) {
            return BotDecision.bid(nextBid);
        }

        // 无法合理叫牌就开
        return BotDecision.challenge();
    }

    /**
     * 做出开局叫牌
     */
    private static Bid makeOpeningBid(List<Integer> myDice, int totalDice) {
        // 统计自己各点数的数量
        Map<Integer, Integer> myCount = countFaces(myDice);

        // 找到自己最多的点数（不算1）
        int bestFace = 2;
        int bestCount = 0;
        for (int face = 2; face <= 6; face++) {
            int count = myCount.getOrDefault(face, 0) + myCount.getOrDefault(1, 0);
            if (count > bestCount) {
                bestCount = count;
                bestFace = face;
            }
        }

        // 预估总数：自己的数量 + 其他人的期望值
        int otherDice = totalDice - 5;
        double expectedPerFace = otherDice / 6.0;
        double expectedOnes = otherDice / 6.0;
        int estimatedTotal = (int) Math.ceil(bestCount + expectedPerFace + expectedOnes);

        // 叫一个合理的起始数
        int callCount = Math.max(estimatedTotal - 1, 1 + totalDice / 10);
        callCount = Math.min(callCount, totalDice);

        return new Bid(callCount, bestFace);
    }

    /**
     * 估算当前叫牌为真的概率
     */
    private static double estimateProbability(List<Integer> myDice, Bid bid, int totalDice) {
        int face = bid.getFace();
        int requiredCount = bid.getCount();

        // 统计自己有多少匹配的骰子
        int myMatchCount = 0;
        for (int val : myDice) {
            if (val == face || (face != 1 && val == 1)) {
                myMatchCount++;
            }
        }
        if (face == 1) {
            myMatchCount = 0;
            for (int val : myDice) {
                if (val == 1) myMatchCount++;
            }
        }

        int remaining = requiredCount - myMatchCount;
        if (remaining <= 0) return 0.95; // 自己就够，大概率是真的

        int otherDice = totalDice - myDice.size();

        // 每颗骰子匹配的概率
        double matchProb;
        if (face == 1) {
            matchProb = 1.0 / 6.0;
        } else {
            matchProb = 2.0 / 6.0; // 本身点数 + 1点万能
        }

        // 使用正态近似计算概率
        double mean = otherDice * matchProb;
        double variance = otherDice * matchProb * (1 - matchProb);
        double stddev = Math.sqrt(variance);

        if (stddev == 0) return remaining <= mean ? 0.9 : 0.1;

        // 需要 remaining 个匹配，计算 P(X >= remaining)
        double z = (remaining - mean) / stddev;
        double probability = 1.0 - normalCDF(z);

        return probability;
    }

    /**
     * 标准正态分布累积函数
     */
    private static double normalCDF(double z) {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    /**
     * 误差函数近似
     */
    private static double erf(double x) {
        double t = 1.0 / (1.0 + 0.3275911 * Math.abs(x));
        double y = 1.0 - (((((1.061405429 * t - 1.453152027) * t) + 1.421413741) * t
                - 0.284496736) * t + 0.254829592) * t * Math.exp(-x * x);
        return x >= 0 ? y : -y;
    }

    /**
     * 获取开牌阈值 - 概率低于此值就开牌
     */
    private static double getChallengeThreshold(int roundNumber, Bid currentBid, Player lastBidder, Player bot) {
        double base = 0.35;

        // 叫牌数量越大，越倾向开牌
        int totalDice = 5 * 4; // 近似
        double ratio = (double) currentBid.getCount() / totalDice;
        if (ratio > 0.6) base -= 0.1;
        if (ratio > 0.8) base -= 0.1;

        // 回合越晚，越倾向开牌
        if (roundNumber > 5) base -= 0.05;

        // 加一些随机性
        base += (RANDOM.nextDouble() - 0.5) * 0.2;

        return Math.max(0.1, Math.min(0.6, base));
    }

    /**
     * 计算下一个叫牌
     */
    private static Bid calculateNextBid(List<Integer> myDice, Bid currentBid,
                                         int totalDice, double probability) {
        int currentCount = currentBid.getCount();
        int currentFace = currentBid.getFace();

        // 策略1：同点数加1
        Bid sameHigher = new Bid(currentCount + 1, currentFace);

        // 策略2：换更高的点数（如果数量足够）
        List<Bid> candidates = new ArrayList<>();

        // 同点数加1
        if (probability > 0.4) {
            candidates.add(sameHigher);
        }

        // 跳数叫（数量+2）
        if (probability > 0.6) {
            candidates.add(new Bid(currentCount + 2, currentFace));
        }

        // 换点数（数量相同，点数+1）
        if (currentFace < 6) {
            Bid higherFace = new Bid(currentCount, currentFace + 1);
            if (higherFace.isValidAgainst(currentBid)) {
                candidates.add(higherFace);
            }
        }

        // 叫1（万能牌重置）
        if (currentFace != 1) {
            // 需要数量*2 >= 当前数量
            int onesNeeded = (int) Math.ceil(currentCount / 2.0);
            Bid onesBid = new Bid(onesNeeded, 1);
            if (onesBid.isValidAgainst(currentBid) && myDice.stream().filter(d -> d == 1).count() >= 1) {
                candidates.add(onesBid);
            }
        }

        // 找到自己最优的点数叫法
        Map<Integer, Integer> myCount = countFaces(myDice);
        int ones = myCount.getOrDefault(1, 0);
        for (int face = 2; face <= 6; face++) {
            int myMatch = myCount.getOrDefault(face, 0) + ones;
            if (myMatch >= 2 && face != currentFace) {
                int needed = currentCount;
                if (face > currentFace) {
                    // 同数量高点数
                } else {
                    needed = currentCount + 1;
                }
                Bid smartBid = new Bid(needed, face);
                if (smartBid.isValidAgainst(currentBid)) {
                    candidates.add(smartBid);
                }
            }
        }

        // 过滤合法叫牌
        candidates.removeIf(b -> !b.isValidAgainst(currentBid));

        if (candidates.isEmpty()) {
            // 只能简单加1
            return sameHigher.isValidAgainst(currentBid) ? sameHigher : null;
        }

        // 从候选中随机选一个（增加不可预测性）
        return candidates.get(RANDOM.nextInt(candidates.size()));
    }

    /**
     * 统计各点数数量
     */
    private static Map<Integer, Integer> countFaces(List<Integer> dice) {
        Map<Integer, Integer> count = new HashMap<>();
        for (int val : dice) {
            count.merge(val, 1, Integer::sum);
        }
        return count;
    }
}