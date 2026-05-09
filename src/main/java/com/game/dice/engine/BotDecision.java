package com.game.dice.engine;

import com.game.dice.model.Bid;

/**
 * 机器人决策结果
 */
public class BotDecision {
    private final boolean challenge;
    private final Bid bid;

    private BotDecision(boolean challenge, Bid bid) {
        this.challenge = challenge;
        this.bid = bid;
    }

    public static BotDecision challenge() {
        return new BotDecision(true, null);
    }

    public static BotDecision bid(Bid bid) {
        return new BotDecision(false, bid);
    }

    public boolean isChallenge() { return challenge; }
    public Bid getBid() { return bid; }
}