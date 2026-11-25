package com.relootmod.data;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public enum RarityTier {
    COMMUN("Commun", ChatFormatting.GRAY, false, 40.0),
    PEU_COMMUN("Peu commun", ChatFormatting.GREEN, false, 30.0),
    RARE("Rare", ChatFormatting.AQUA, false, 18.0),
    EPIQUE("Épique", ChatFormatting.LIGHT_PURPLE, false, 8.5),
    LEGENDAIRE("Légendaire", ChatFormatting.GOLD, true, 3.0),
    MYTHIQUE("Mythique", ChatFormatting.DARK_RED, true, 0.5);

    private final String displayName;
    private final ChatFormatting color;
    private final boolean bold;
    private final double defaultWeight;

    RarityTier(String displayName, ChatFormatting color, boolean bold, double defaultWeight) {
        this.displayName = displayName;
        this.color = color;
        this.bold = bold;
        this.defaultWeight = defaultWeight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public boolean isBold() {
        return bold;
    }

    public double getDefaultWeight() {
        return defaultWeight;
    }

    public MutableComponent getFormattedName(String itemName) {
        Style style = Style.EMPTY.withColor(color).withBold(bold);
        return Component.literal(itemName).setStyle(style);
    }

    public List<Component> createLore() {
        List<Component> lore = new ArrayList<>();
        lore.add(Component.literal("§8━━━━━━━━━━━━━━━━━━"));
        Style style = Style.EMPTY.withColor(color).withBold(bold);
        lore.add(Component.literal("Rareté: ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(displayName).setStyle(style)));
        lore.add(Component.literal("§8━━━━━━━━━━━━━━━━━━"));
        return lore;
    }

    public static RarityTier rollRarity(RandomSource random) {
        double roll = random.nextDouble() * 100.0;
        if (roll <= MYTHIQUE.defaultWeight) {
            return MYTHIQUE;
        }
        roll -= MYTHIQUE.defaultWeight;
        if (roll <= LEGENDAIRE.defaultWeight) {
            return LEGENDAIRE;
        }
        roll -= LEGENDAIRE.defaultWeight;
        if (roll <= EPIQUE.defaultWeight) {
            return EPIQUE;
        }
        roll -= EPIQUE.defaultWeight;
        if (roll <= RARE.defaultWeight) {
            return RARE;
        }
        roll -= RARE.defaultWeight;
        if (roll <= PEU_COMMUN.defaultWeight) {
            return PEU_COMMUN;
        }
        return COMMUN;
    }
}
