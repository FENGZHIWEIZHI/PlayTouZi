package com.game.dice.engine;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * LLM API 客户端 - 兼容 OpenAI 接口协议
 * 用于调用 mimo-v2.5-pro 模型
 */
public class LLMClient {

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * 调用 LLM API 获取回复
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户消息
     * @return 模型回复文本，失败返回 null
     */
    public static String chat(String systemPrompt, String userPrompt) {
        AIConfig config = AIConfig.getInstance();

        if (!config.isApiKeyConfigured()) {
            System.err.println("API Key 未配置");
            return null;
        }

        try {
            // 构建请求体 (OpenAI 格式)
            JsonObject requestBody = new JsonObject();
            requestBody.addProperty("model", config.getModel());
            requestBody.addProperty("temperature", config.getTemperature());
            requestBody.addProperty("max_tokens", config.getMaxTokens());

            JsonArray messages = new JsonArray();

            if (systemPrompt != null && !systemPrompt.isBlank()) {
                JsonObject systemMsg = new JsonObject();
                systemMsg.addProperty("role", "system");
                systemMsg.addProperty("content", systemPrompt);
                messages.add(systemMsg);
            }

            JsonObject userMsg = new JsonObject();
            userMsg.addProperty("role", "user");
            userMsg.addProperty("content", userPrompt);
            messages.add(userMsg);

            requestBody.add("messages", messages);

            String jsonBody = GSON.toJson(requestBody);

            // 构建 HTTP 请求
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getApiUrl() + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .timeout(Duration.ofMillis(config.getTimeoutMs()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // 发送请求
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("LLM API 调用失败, HTTP " + response.statusCode() + ": " + response.body());
                return null;
            }

            // 解析响应 (OpenAI 格式)
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray choices = jsonResponse.getAsJsonArray("choices");

            if (choices != null && choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                return message.get("content").getAsString().trim();
            }

            return null;

        } catch (Exception e) {
            System.err.println("LLM API 调用异常: " + e.getMessage());
            return null;
        }
    }

    /**
     * 快速调用（只有用户消息，无系统提示）
     */
    public static String chat(String userPrompt) {
        return chat(null, userPrompt);
    }

    /**
     * 测试 API 连接
     * @return true 如果连接成功
     */
    public static boolean testConnection() {
        String response = chat("你好，请回复OK");
        return response != null && !response.isBlank();
    }
}