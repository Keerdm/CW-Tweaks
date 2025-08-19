package com.keerdm.cw_tweaks.data;

import net.minecraft.util.RandomSource;

public enum LootTier {
    TIER_1(0, 35, 0.0f, "cw_tweaks:chests/tier_1"),
    TIER_2(36, 60, 0.3f, "cw_tweaks:chests/tier_2"),
    TIER_3(61, 78, 0.5f, "cw_tweaks:chests/tier_3"),
    TIER_4(79, 90, 0.7f, "cw_tweaks:chests/tier_4"),
    TIER_5(91, 97, 0.85f, "cw_tweaks:chests/tier_5"),
    TIER_6(98, 100, 0.95f, "cw_tweaks:chests/tier_6");

    private final int minWeight;
    private final int maxWeight;
    private final float lockChance;
    private final String lootTableId;

    LootTier(int minWeight, int maxWeight, float lockChance, String lootTableId) {
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.lockChance = lockChance;
        this.lootTableId = lootTableId;
    }

    public int getMinWeight() {
        return minWeight;
    }

    public int getMaxWeight() {
        return maxWeight;
    }

    public float getLockChance() {
        return lockChance;
    }

    public String getLootTableId() {
        return lootTableId;
    }

    public boolean shouldHaveLock(RandomSource randomSource) {
        return randomSource.nextFloat() < lockChance;
    }

    public static LootTier fromWeight(int weight) {
        for (LootTier tier : values()) {
            if (weight >= tier.minWeight && weight <= tier.maxWeight) {
                return tier;
            }
        }
        return TIER_1;
    }

    public LockType getLockType() {
        return switch (this) {
            case TIER_1 -> LockType.NONE;
            case TIER_2 -> LockType.WOOD;
            case TIER_3 -> LockType.GOLD;
            case TIER_4 -> LockType.IRON;
            case TIER_5 -> LockType.STEEL;
            case TIER_6 -> LockType.DIAMOND;
        };
    }
}