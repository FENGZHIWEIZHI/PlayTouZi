package com.game.dice.model;

import java.util.*;

/**
 * 皮肤管理器 - 骰盅和台面皮肤
 */
public class SkinManager {

    public enum SkinType {
        // 台面皮肤
        CLASSIC_GREEN("经典绿", "默认台面", "🟢", SkinCategory.TABLE, null),
        OCEAN_BLUE("海洋蓝", "深海风格台面", "🔵", SkinCategory.TABLE, Achievement.AchievementType.VETERAN),
        ROYAL_PURPLE("皇家紫", "奢华风格台面", "🟣", SkinCategory.TABLE, Achievement.AchievementType.WIN_STREAK_5),
        FIRE_RED("烈焰红", "热血风格台面", "🔴", SkinCategory.TABLE, Achievement.AchievementType.PSYCH_MASTER),
        GOLD_LUXURY("黄金奢华", "顶级台面皮肤", "🟡", SkinCategory.TABLE, Achievement.AchievementType.WIN_STREAK_7),

        // 骰盅皮肤
        WOODEN_CUP("木杯", "默认骰盅", "🪵", SkinCategory.CUP, null),
        GOLDEN_CUP("金杯", "闪亮金杯", "🏆", SkinCategory.CUP, Achievement.AchievementType.PRECISION_MACHINE),
        CRYSTAL_CUP("水晶杯", "透明水晶骰盅", "💎", SkinCategory.CUP, Achievement.AchievementType.CHALLENGE_MASTER),
        DRAGON_CUP("龙纹杯", "龙纹雕刻骰盅", "🐉", SkinCategory.CUP, Achievement.AchievementType.ONES_MASTER),
        JADE_CUP("翡翠杯", "珍稀翡翠骰盅", "🟢", SkinCategory.CUP, Achievement.AchievementType.TIME_MANAGER);

        private final String displayName;
        private final String description;
        private final String icon;
        private final SkinCategory category;
        private final Achievement.AchievementType requiredAchievement;

        SkinType(String displayName, String description, String icon,
                 SkinCategory category, Achievement.AchievementType requiredAchievement) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.category = category;
            this.requiredAchievement = requiredAchievement;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getIcon() { return icon; }
        public SkinCategory getCategory() { return category; }
        public Achievement.AchievementType getRequiredAchievement() { return requiredAchievement; }
    }

    public enum SkinCategory {
        TABLE("台面"),
        CUP("骰盅");

        private final String displayName;
        SkinCategory(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
    }

    private SkinType currentTableSkin = SkinType.CLASSIC_GREEN;
    private SkinType currentCupSkin = SkinType.WOODEN_CUP;
    private final Set<SkinType> unlockedSkins = new HashSet<>();

    public SkinManager() {
        // 默认皮肤自动解锁
        unlockedSkins.add(SkinType.CLASSIC_GREEN);
        unlockedSkins.add(SkinType.WOODEN_CUP);
    }

    /**
     * 根据成就解锁皮肤
     */
    public static SkinType getSkinForAchievement(Achievement.AchievementType achievement) {
        for (SkinType skin : SkinType.values()) {
            if (skin.getRequiredAchievement() == achievement) {
                return skin;
            }
        }
        return null;
    }

    /**
     * 解锁皮肤
     */
    public void unlockSkin(SkinType skin) {
        unlockedSkins.add(skin);
    }

    /**
     * 选择台面皮肤
     */
    public boolean selectTableSkin(SkinType skin) {
        if (skin.getCategory() == SkinCategory.TABLE && unlockedSkins.contains(skin)) {
            currentTableSkin = skin;
            return true;
        }
        return false;
    }

    /**
     * 选择骰盅皮肤
     */
    public boolean selectCupSkin(SkinType skin) {
        if (skin.getCategory() == SkinCategory.CUP && unlockedSkins.contains(skin)) {
            currentCupSkin = skin;
            return true;
        }
        return false;
    }

    /**
     * 获取台面背景色
     */
    public String getTableBackgroundStyle() {
        return switch (currentTableSkin) {
            case CLASSIC_GREEN -> "-fx-background-color: linear-gradient(to bottom, #1a5c2e, #0d3318);";
            case OCEAN_BLUE -> "-fx-background-color: linear-gradient(to bottom, #1a3c5c, #0a1e33);";
            case ROYAL_PURPLE -> "-fx-background-color: linear-gradient(to bottom, #3c1a5c, #1e0a33);";
            case FIRE_RED -> "-fx-background-color: linear-gradient(to bottom, #5c1a1a, #330a0a);";
            case GOLD_LUXURY -> "-fx-background-color: linear-gradient(to bottom, #5c4a1a, #33280a);";
            default -> "-fx-background-color: linear-gradient(to bottom, #1a1a2e, #16213e, #0f3460);";
        };
    }

    public SkinType getCurrentTableSkin() { return currentTableSkin; }
    public SkinType getCurrentCupSkin() { return currentCupSkin; }
    public Set<SkinType> getUnlockedSkins() { return unlockedSkins; }

    public List<SkinType> getUnlockedByCategory(SkinCategory category) {
        List<SkinType> list = new ArrayList<>();
        for (SkinType skin : unlockedSkins) {
            if (skin.getCategory() == category) list.add(skin);
        }
        return list;
    }

    public List<SkinType> getLockedByCategory(SkinCategory category) {
        List<SkinType> list = new ArrayList<>();
        for (SkinType skin : SkinType.values()) {
            if (skin.getCategory() == category && !unlockedSkins.contains(skin)) {
                list.add(skin);
            }
        }
        return list;
    }
}