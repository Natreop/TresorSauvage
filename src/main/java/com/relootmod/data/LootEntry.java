package com.relootmod.data;

public class LootEntry {
    private String itemId;
    private int minCount;
    private int maxCount;
    private String customName;
    private RarityTier rarity;

    public LootEntry(String itemId, int minCount, int maxCount, String customName, RarityTier rarity) {
        this.itemId = itemId;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.customName = customName;
        this.rarity = rarity;
    }

    public String getItemId() {
        return itemId;
    }

    public int getMinCount() {
        return minCount;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public String getCustomName() {
        return customName;
    }

    public RarityTier getRarity() {
        return rarity;
    }
}
