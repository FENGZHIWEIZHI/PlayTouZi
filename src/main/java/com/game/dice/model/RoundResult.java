package com.game.dice.model;

import java.util.List;
import java.util.Map;

/**
 * 回合结果类 - 记录一局的结算信息
 */
public class RoundResult {
    private final Player caller;        // 叫牌者
    private final Player challenger;    // 开牌者
    private final Bid lastBid;          // 最后的叫牌
    private final int actualCount;      // 实际数量
    private final boolean callerWins;   // 叫牌者是否赢
    private final Map<Player, List<Integer>> allDiceValues; // 所有玩家骰子
    private final String bonusMessage;  // 额外奖励消息

    public RoundResult(Player caller, Player challenger, Bid lastBid,
                       int actualCount, boolean callerWins,
                       Map<Player, List<Integer>> allDiceValues, String bonusMessage) {
        this.caller = caller;
        this.challenger = challenger;
        this.lastBid = lastBid;
        this.actualCount = actualCount;
        this.callerWins = callerWins;
        this.allDiceValues = allDiceValues;
        this.bonusMessage = bonusMessage;
    }

    public Player getCaller() { return caller; }
    public Player getChallenger() { return challenger; }
    public Bid getLastBid() { return lastBid; }
    public int getActualCount() { return actualCount; }
    public boolean isCallerWins() { return callerWins; }
    public Map<Player, List<Integer>> getAllDiceValues() { return allDiceValues; }
    public String getBonusMessage() { return bonusMessage; }

    public Player getWinner() { return callerWins ? caller : challenger; }
    public Player getLoser() { return callerWins ? challenger : caller; }
}