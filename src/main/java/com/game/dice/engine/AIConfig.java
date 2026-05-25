package com.game.dice.engine;

import java.io.*;
import java.util.Properties;

/**
 * AI配置类 - 管理LLM API的配置信息
 * 配置文件保存在用户目录下的 .playtouzi/config.properties
 */
public class AIConfig {

    public enum AIMode {
        RULE_BASED("规则AI"),
        LLM("AI大模型"),
        HYBRID("混合模式");

        private final String displayName;

        AIMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // 默认配置
    private static final String DEFAULT_API_URL = "https://token-plan-cn.xiaomimimo.com/v1";
    private static final String DEFAULT_MODEL = "mimo-v2.5-pro";
    private static final int DEFAULT_TIMEOUT = 15000;
    private static final int DEFAULT_MAX_TOKENS = 512;

    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".playtouzi";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "config.properties";

    private static AIConfig instance;

    // 配置项
    private String apiUrl;
    private String apiKey;
    private String model;
    private int timeoutMs;
    private int maxTokens;
    private AIMode aiMode;
    private double temperature;

    private AIConfig() {
        loadDefaults();
        loadFromFile();
    }

    public static synchronized AIConfig getInstance() {
        if (instance == null) {
            instance = new AIConfig();
        }
        return instance;
    }

    private void loadDefaults() {
        this.apiUrl = DEFAULT_API_URL;
        this.apiKey = "";
        this.model = DEFAULT_MODEL;
        this.timeoutMs = DEFAULT_TIMEOUT;
        this.maxTokens = DEFAULT_MAX_TOKENS;
        this.aiMode = AIMode.RULE_BASED;
        this.temperature = 0.7;
    }

    /**
     * 从配置文件加载
     */
    private void loadFromFile() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return;

        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);

            String url = props.getProperty("ai.api.url");
            if (url != null && !url.isBlank()) this.apiUrl = url;

            String key = props.getProperty("ai.api.key");
            if (key != null && !key.isBlank()) this.apiKey = key;

            String mdl = props.getProperty("ai.model");
            if (mdl != null && !mdl.isBlank()) this.model = mdl;

            String timeout = props.getProperty("ai.timeout");
            if (timeout != null && !timeout.isBlank()) {
                try { this.timeoutMs = Integer.parseInt(timeout); } catch (NumberFormatException ignored) {}
            }

            String tokens = props.getProperty("ai.max_tokens");
            if (tokens != null && !tokens.isBlank()) {
                try { this.maxTokens = Integer.parseInt(tokens); } catch (NumberFormatException ignored) {}
            }

            String mode = props.getProperty("ai.mode");
            if (mode != null && !mode.isBlank()) {
                try { this.aiMode = AIMode.valueOf(mode); } catch (IllegalArgumentException ignored) {}
            }

            String temp = props.getProperty("ai.temperature");
            if (temp != null && !temp.isBlank()) {
                try { this.temperature = Double.parseDouble(temp); } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.err.println("读取AI配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存配置到文件
     */
    public void saveToFile() {
        File dir = new File(CONFIG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Properties props = new Properties();
        props.setProperty("ai.api.url", apiUrl != null ? apiUrl : "");
        props.setProperty("ai.api.key", apiKey != null ? apiKey : "");
        props.setProperty("ai.model", model != null ? model : "");
        props.setProperty("ai.timeout", String.valueOf(timeoutMs));
        props.setProperty("ai.max_tokens", String.valueOf(maxTokens));
        props.setProperty("ai.mode", aiMode.name());
        props.setProperty("ai.temperature", String.valueOf(temperature));

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            props.store(fos, "PlayTouZi AI Configuration");
        } catch (IOException e) {
            System.err.println("保存AI配置文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查API Key是否已配置
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * 检查LLM是否可用
     */
    public boolean isLLMAvailable() {
        return (aiMode == AIMode.LLM || aiMode == AIMode.HYBRID) && isApiKeyConfigured();
    }

    // ==================== Getters & Setters ====================

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getTimeoutMs() { return timeoutMs; }
    public void setTimeoutMs(int timeoutMs) { this.timeoutMs = timeoutMs; }

    public int getMaxTokens() { return maxTokens; }
    public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }

    public AIMode getAiMode() { return aiMode; }
    public void setAiMode(AIMode aiMode) { this.aiMode = aiMode; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
}