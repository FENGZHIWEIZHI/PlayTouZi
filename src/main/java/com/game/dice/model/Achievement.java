package com.game.dice.model;

import java.util.*;

/**
 * 成就系统
 */
public class Achievement {

    public enum AchievementType {
        PSYCH_MASTER("心理大师", "一局中成功诈唬3次", "🧠", 3),
        PRECISION_MACHINE("精准机器", "累计10次精准叫牌", "🎯", 10),
        TIME_MANAGER("时间管理者", "连续30局不超时", "⏰", 30),
        BLUFF_HUNTER("诈唬猎人", "累计识破5次诈唬", "🔍", 5),
        ONES_MASTER("一点通", "累计使用10次1点战术", "⚀", 10),
        WIN_STREAK_3("三连胜", "达成3连胜", "🔥", 3),
        WIN_STREAK_5("五连胜", "达成5连胜", "🔥🔥", 5),
        WIN_STREAK_7("七连胜", "达成7连胜", "🔥🔥🔥", 7),
        VETERAN("老兵", "累计完成50局", "⭐", 50),
        CHALLENGE_MASTER("开牌大师", "累计成功开牌20次", "🏆", 20);

        private final String displayName;
        private final String description;
        private final String icon;
        private final int target;

        AchievementType(String displayName, String description, String icon, int target) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.target = target;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public int getTarget() { return target; }
    }

    /**
     * 成就进度
     */
    public static class AchievementProgress {
        public final AchievementType type;
        public int progress;
        public boolean unlocked;

        public AchievementProgress(AchievementType type) {
            this.type = type;
            this.progress = 0;
            this.unlocked = false;
        }

        public void addProgress(int amount) {
            if (!unlocked) {
                progress += amount;
                if (progress >= type.getTarget()) {
                    progress = type.getTarget();
                    unlocked = true;
                }
            }
        }

        public double getPercentage() {
            return Math.min(100.0, progress * 100.0 / type.getTarget());
        }
    }

    private final Map<AchievementType, AchievementProgress> achievements = new LinkedHashMap<>();
    private final List<String> newlyUnlocked = new ArrayList<>();

    public Achievement() {
        for (AchievementType type : AchievementType.values()) {
            achievements.put(type, new AchievementProgress(type));
        }
    }

    /**
     * 更新成就进度
     */
    public List<String> updateProgress(GameStats.PlayerStats stats, int winStreak) {
        newlyUnlocked.clear();
        List<String> notifications = new ArrayList<>();

        // 心理大师：一局中成功诈唬3次
        checkAndNotify(AchievementType.PSYCH_MASTER, stats.successfulBluffs);

        // 精准机器：累计精准叫牌
        checkAndNotify(AchievementType.PRECISION_MACHINE, stats.preciseCalls);

        // 时间管理者：连续不超时
        checkAndNotify(AchievementType.TIME_MANAGER, stats.consecutiveNoTimeout);

        // 诈唬猎人：识破诈唬
        checkAndNotify(AchievementType.BLUFF_HUNTER, stats.bluffsCaught);

        // 一点通：1点战术
        checkAndNotify(AchievementType.ONES_MASTER, stats.onesTactics);

        // 连胜成就
        checkAndNotify(AchievementType.WIN_STREAK_3, winStreak);
        checkAndNotify(AchievementType.WIN_STREAK_5, winStreak);
        checkAndNotify(AchievementType.WIN_STREAK_7, winStreak);

        // 老兵：总局数
        checkAndNotify(AchievementType.VETERAN, stats.totalCalls + stats.totalChallenges);

        // 开牌大师
        checkAndNotify(AchievementType.CHALLENGE_MASTER, stats.successfulChallenges);

        for (String name : newlyUnlocked) {
            notifications.add("🏆 成就解锁: " + name + "!");
        }
        return notifications;
    }

    private void checkAndNotify(AchievementType type, int current) {
        AchievementProgress prog = achievements.get(type);
        boolean wasUnlocked = prog.unlocked;
        prog.addProgress(current - prog.progress);
        if (prog.unlocked && !wasUnlocked) {
            newlyUnlocked.add(type.getIcon() + " " + type.getDisplayName());
        }
    }

    public Map<AchievementType, AchievementProgress> getAchievements() {
        return achievements;
    }

    /**
     * 获取已解锁成就列表
     */
    public List<AchievementType> getUnlocked() {
        List<AchievementType> list = new ArrayList<>();
        for (var entry : achievements.entrySet()) {
            if (entry.getValue().unlocked) list.add(entry.getKey());
        }
        return list;
    }

    /**
     * 获取已解锁成就对应的皮肤列表
     */
    public List<SkinManager.SkinType> getUnlockedSkins() {
        List<SkinManager.SkinType> skins = new ArrayList<>();
        for (AchievementType ach : getUnlocked()) {
            SkinManager.SkinType skin = SkinManager.getSkinForAchievement(ach);
            if (skin != null) skins.add(skin);
        }
        return skins;
    }
}