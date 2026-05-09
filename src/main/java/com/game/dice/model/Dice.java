package com.game.dice.model;

import java.util.Random;

/**
 * 骰子类
 */
public class Dice {
    private static final Random RANDOM = new Random();
    private int value;

    public Dice() {
        roll();
    }

    /**
     * 掷骰子
     */
    public void roll() {
        value = RANDOM.nextInt(6) + 1;
    }

    public int getValue() {
        return value;
    }

    /**
     * 获取骰子的Unicode显示
     */
    public String getUnicodeFace() {
        return switch (value) {
            case 1 -> "\u2680"; // ⚀
            case 2 -> "\u2681"; // ⚁
            case 3 -> "\u2682"; // ⚂
            case 4 -> "\u2683"; // ⚃
            case 5 -> "\u2684"; // ⚄
            case 6 -> "\u2685"; // ⚅
            default -> "?";
        };
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}