package com.game.dice.engine;

import com.game.dice.model.Bid;
import com.game.dice.model.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM增强版机器人AI策略
 * 通过调用大语言模型来做决策，具备更强的推理能力
 */
public class LLMBotAI {

    private static final String SYSTEM_PROMPT = """
            你是一个吹牛骰子（Liar's Dice）游戏的AI玩家。你需要根据当前游戏局面做出决策。
            
            【游戏规则】
            - 每人5颗骰子，只能看到自己的骰子
            - 1点是万能牌，可以代表任何点数
            - 轮流叫牌，叫牌必须比上家更高（数量更多或同数量点数更大）
            - 从普通叫到1点：数量×2 >= 上家数量
            - 从1点叫到普通：数量 > 上家数量×2
            - 不相信上家可以喊"开"（质疑），所有人亮出骰子验证
            - 实际数量达到叫牌数量→开牌者输，反之→叫牌者输
            
            【你的任务】
            分析局面后做出最优决策。你需要输出严格的JSON格式：
            
            如果选择叫牌：
            {"action": "bid", "count": 数量, "face": 点数, "reason": "简短理由"}
            
            如果选择开牌（质疑）：
            {"action": "challenge", "reason": "简短理由"}
            
            只输出JSON，不要输出其他内容。
            """;

    private static final Random RANDOM = new Random();

    /**
     * 使用LLM为机器人做出决策
     * 如果LLM调用失败，回退到规则AI
     */
    public static BotDecision makeDecision(Player bot, Bid currentBid, Player lastBidder,
                                           List<Player> allPlayers, int roundNumber) {
        try {
            String prompt = buildGamePrompt(bot, currentBid, lastBidder, allPlayers, roundNumber);
            String response = LLMClient.chat(SYSTEM_PROMPT, prompt);

            if (response != null) {
                BotDecision decision = parseLLMResponse(response, currentBid);
                if (decision != null) {
                    return decision;
                }
            }
        } catch (Exception e) {
            System.err.println("LLM决策异常，回退到规则AI: " + e.getMessage());
        }

        // 回退到规则AI
        return BotAI.makeDecision(bot, currentBid, lastBidder, allPlayers, roundNumber);
    }

    /**
     * 构建发送给LLM的游戏状态prompt
     */
    private static String buildGamePrompt(Player bot, Bid currentBid, Player lastBidder,
                                           List<Player> allPlayers, int roundNumber) {
        StringBuilder sb = new StringBuilder();

        sb.append("【当前局面】\n");
        sb.append("回合数: ").append(roundNumber).append("\n");
        sb.append("你是: ").append(bot.getName()).append("\n");
        sb.append("你的骰子: ").append(formatDice(bot.getDiceValues())).append("\n\n");

        // 其他玩家信息（不含骰子，看不到）
        sb.append("其他玩家:\n");
        for (Player p : allPlayers) {
            if (!p.isEliminated() && p != bot) {
                sb.append("  - ").append(p.getName())
                  .append(" (剩余").append(p.getDices().size()).append("颗骰子)\n");
            }
        }
        sb.append("\n");

        // 当前叫牌情况
        if (currentBid != null) {
            sb.append("当前叫牌: ").append(currentBid.getDisplayText()).append("\n");
            if (lastBidder != null) {
                sb.append("叫牌者: ").append(lastBidder.getName()).append("\n");
            }
            // 概率参考
            int totalDice = allPlayers.stream()
                    .filter(p -> !p.isEliminated())
                    .mapToInt(p -> p.getDices().size())
                    .sum();
            double prob = ProbabilityCalculator.calculateProbability(
                    bot.getDiceValues(), currentBid, totalDice);
            sb.append("当前叫牌成立的概率约为: ").append(String.format("%.0f%%", prob * 100)).append("\n");
        } else {
            sb.append("当前无叫牌，你是第一个叫牌的人。\n");
        }

        sb.append("\n请做出你的决策（严格输出JSON）:");

        return sb.toString();
    }

    /**
     * 格式化骰子显示
     */
    private static String formatDice(List<Integer> dice) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < dice.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(dice.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 解析LLM返回的JSON决策
     */
    private static BotDecision parseLLMResponse(String response, Bid currentBid) {
        try {
            // 尝试从回复中提取JSON（LLM可能在JSON前后加了其他文字）
            String jsonStr = extractJson(response);
            if (jsonStr == null) return null;

            // 简单解析JSON（不依赖复杂解析）
            String action = extractJsonField(jsonStr, "action");

            if ("challenge".equalsIgnoreCase(action)) {
                return BotDecision.challenge();
            } else if ("bid".equalsIgnoreCase(action)) {
                String countStr = extractJsonField(jsonStr, "count");
                String faceStr = extractJsonField(jsonStr, "face");

                if (countStr != null && faceStr != null) {
                    int count = Integer.parseInt(countStr.trim());
                    int face = Integer.parseInt(faceStr.trim());

                    // 验证合法性
                    if (face < 1 || face > 6 || count < 1) {
                        System.err.println("LLM返回无效的叫牌: " + count + "个" + face);
                        return null;
                    }

                    Bid newBid = new Bid(count, face);
                    if (currentBid != null && !newBid.isValidAgainst(currentBid)) {
                        System.err.println("LLM返回的叫牌不合法: " + newBid.getDisplayText()
                                + " (当前叫牌: " + currentBid.getDisplayText() + ")");
                        return null;
                    }

                    return BotDecision.bid(newBid);
                }
            }
        } catch (Exception e) {
            System.err.println("解析LLM响应失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 从文本中提取JSON字符串
     */
    private static String extractJson(String text) {
        // 尝试匹配 { ... } 格式的JSON
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return null;
    }

    /**
     * 简单提取JSON字段值
     */
    private static String extractJsonField(String json, String fieldName) {
        // 匹配 "fieldName": "value" 或 "fieldName": value
        Pattern pattern = Pattern.compile("\"" + fieldName + "\"\\s*:\\s*\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}