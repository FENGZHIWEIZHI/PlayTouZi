package com.game.dice.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 玩家类
 */
public class Player {
    private final String name;
    private final boolean isHuman;
    private final List<Dice> dices;
    private int score;
    private int consecutiveWins;
    private int timeoutCount;
    private boolean eliminated;

    public Player(String name, boolean isHuman) {
        this.name = name;
        this.isHuman = isHuman;
        this.dices = new ArrayList<>();
        this.score = 0;
        this.consecutiveWins = 0;
        this.timeoutCount = 0;
        this.eliminated = false;
        // 初始化5颗骰子
        for (int i = 0; i < 5; i++) {
            dices.add(new Dice());
        }
    }

    public String getName() {
        return name;
    }

    public boolean isHuman() {
        return isHuman;
    }

    public List<Dice> getDices() {
        return dices;
    }

    /**
     * 获取骰子点数列表
     */
    public List<Integer> getDiceValues() {
        List<Integer> values = new ArrayList<>();
        for (Dice d : dices) {
            values.add(d.getValue());
        }
        return values;
    }

    /**
     * 重新掷所有骰子
     */
    public void rollAllDices() {
        for (Dice d : dices) {
            d.roll();
        }
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void addScore(int delta) {
        this.score += delta;
    }

    public int getConsecutiveWins() {
        return consecutiveWins;
    }

    public void setConsecutiveWins(int consecutiveWins) {
        this.consecutiveWins = consecutiveWins;
    }

    public void incrementConsecutiveWins() {
        this.consecutiveWins++;
    }

    public void resetConsecutiveWins() {
        this.consecutiveWins = 0;
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    public void incrementTimeoutCount() {
        this.timeoutCount++;
    }

    public void resetTimeoutCount() {
        this.timeoutCount = 0;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }

    /**
     * 统计指定点数的骰子数量（包括1点作为万能牌）
     */
    public int countFace(int face) {
        int count = 0;
        for (Dice d : dices) {
            if (d.getValue() == face) {
                count++;
            }
        }
        return count;
    }

    /**
     * 统计1点的骰子数量
     */
    public int countOnes() {
        return countFace(1);
    }

    @Override
    public String toString() {
        return name + " (分数:" + score + ")";
    }
}