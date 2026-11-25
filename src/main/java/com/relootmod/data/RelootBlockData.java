package com.relootmod.data;

import net.minecraft.nbt.CompoundTag;

public class RelootBlockData {
    private LootTheme theme;
    private long cooldownSeconds;
    private long lastLootTimeEpochSeconds;

    public RelootBlockData(LootTheme theme, long cooldownSeconds, long lastLootTimeEpochSeconds) {
        this.theme = theme;
        this.cooldownSeconds = cooldownSeconds;
        this.lastLootTimeEpochSeconds = lastLootTimeEpochSeconds;
    }

    public LootTheme getTheme() {
        return theme;
    }

    public void setTheme(LootTheme theme) {
        this.theme = theme;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }

    public long getLastLootTimeEpochSeconds() {
        return lastLootTimeEpochSeconds;
    }

    public void setLastLootTimeEpochSeconds(long lastLootTimeEpochSeconds) {
        this.lastLootTimeEpochSeconds = lastLootTimeEpochSeconds;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Theme", theme.name());
        tag.putLong("Cooldown", cooldownSeconds);
        tag.putLong("LastLoot", lastLootTimeEpochSeconds);
        return tag;
    }

    public static RelootBlockData load(CompoundTag tag) {
        LootTheme theme = LootTheme.fromString(tag.getString("Theme"));
        long cooldown = tag.contains("Cooldown") ? tag.getLong("Cooldown") : 7200L;
        long lastLoot = tag.contains("LastLoot") ? tag.getLong("LastLoot") : 0L;
        return new RelootBlockData(theme, cooldown, lastLoot);
    }
}
