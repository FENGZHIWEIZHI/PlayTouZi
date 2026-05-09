package com.game.dice.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 计分管理器 - 处理所有计分逻辑
 */
public class ScoreManager {

    /**
     * 计算回合结束后的积分变化，返回积分变化消息列表
     */
    public static List<String> applyRoundResult(RoundResult result, GameSettings settings) {
        List<String> messages = new ArrayList<>();
        Player winner = result.getWinner();
        Player loser = result.getLoser();
        Bid bid = result.getLastBid();
        int actual = result.getActualCount();
        int bidCount = bid.getCount();

        // 基础积分
        winner.addScore(1);
        loser.addScore(-1);
        messages.add(winner.getName() + " 获胜 +1分");
        messages.add(loser.getName() + " 失败 -1分");

        // 技巧奖励分
        StringBuilder bonus = new StringBuilder();

        // 1. 精准叫牌: 叫牌数量与实际完全一致
        if (bidCount == actual) {
            if (result.isCallerWins()) {
                winner.addScore(3);
                bonus.append("【精准叫牌】").append(winner.getName()).append(" +3分\n");
            } else {
                // 开牌者开到精准
                loser.addScore(3);
                bonus.append("【精准叫牌】").append(loser.getName()).append(" +3分\n");
            }
        }

        // 2. 极限施压: 叫牌数量比实际多1个
        if (bidCount == actual + 1) {
            if (!result.isCallerWins()) {
                // 叫牌者输了但极限施压
                loser.addScore(1);
                bonus.append("【极限施压】").append(loser.getName()).append(" +1分\n");
            }
        }

        // 3. 成功诈唬: 叫牌数量比实际多3个及以上
        if (bidCount >= actual + 3) {
            if (!result.isCallerWins()) {
                // 开牌者成功识破诈唬
                winner.addScore(1); // 额外+1（总共+2）
                loser.addScore(-1); // 额外-1（总共-2）
                bonus.append("【成功诈唬】").append(winner.getName()).append(" +2分, ")
                      .append(loser.getName()).append(" -2分\n");
            }
        }

        // 4. 1点转换战术
        if (bid.getFace() == 1 && result.isCallerWins()) {
            winner.addScore(1);
            bonus.append("【1点转换战术】").append(winner.getName()).append(" +1分\n");
        }

        // 连胜奖励
        winner.incrementConsecutiveWins();
        loser.resetConsecutiveWins();

        int streak = winner.getConsecutiveWins();
        if (streak == 3) {
            winner.addScore(2);
            bonus.append("【3连胜】").append(winner.getName()).append(" +2分\n");
        } else if (streak == 5) {
            winner.addScore(5);
            bonus.append("【5连胜】").append(winner.getName()).append(" +5分\n");
        } else if (streak == 7) {
            winner.addScore(10);
            bonus.append("【7连胜】").append(winner.getName()).append(" +10分\n");
        }

        if (bonus.length() > 0) {
            messages.add(bonus.toString().trim());
        }

        // 淘汰赛模式检查出局
        if (settings.getGameMode() == GameSettings.GameMode.ELIMINATION) {
            if (loser.getScore() <= 0) {
                loser.setEliminated(true);
                messages.add(loser.getName() + " 分数归零，已被淘汰！");
            }
        }

        return messages;
    }

    /**
     * 处理违规叫牌惩罚
     */
    public static String applyViolationPenalty(Player player) {
        player.addScore(-2);
        return player.getName() + " 违规叫牌 -2分，本轮出局";
    }

    /**
     * 处理超时惩罚
     */
    public static String applyTimeoutPenalty(Player player) {
        player.incrementTimeoutCount();
        player.addScore(-1);
        if (player.getTimeoutCount() >= 3) {
            player.addScore(-3);
            player.resetTimeoutCount();
            return player.getName() + " 累计3次超时 -4分（本次-1 + 累计-3）";
        }
        return player.getName() + " 超时 -1分";
    }

    /**
     * 处理误开惩罚
     */
    public static String applyFalseOpenPenalty(Player player, boolean noBidMade) {
        if (noBidMade) {
            player.addScore(-2);
            return player.getName() + " 无人叫牌时抢开 -2分";
        } else {
            player.addScore(-1);
            return player.getName() + " 非轮次开牌 -1分";
        }
    }
}