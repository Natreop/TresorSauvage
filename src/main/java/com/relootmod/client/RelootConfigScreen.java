package com.relootmod.client;

import com.relootmod.data.LootTheme;
import com.relootmod.network.PacketHandler;
import com.relootmod.network.SaveConfigPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class RelootConfigScreen extends Screen {
    private final BlockPos pos;
    private LootTheme selectedTheme;
    private long selectedCooldown;
    private final boolean isConfigured;

    public RelootConfigScreen(BlockPos pos, String currentTheme, long currentCooldown, boolean isConfigured) {
        super(Component.literal("Configuration du Reloot"));
        this.pos = pos;
        this.selectedTheme = LootTheme.fromString(currentTheme);
        this.selectedCooldown = currentCooldown;
        this.isConfigured = isConfigured;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int startY = 35;

        // Boutons pour les 14 Thèmes de loot (sur 2 colonnes de 7)
        LootTheme[] themes = LootTheme.values();
        for (int i = 0; i < themes.length; i++) {
            LootTheme theme = themes[i];
            int col = i % 2;
            int row = i / 2;
            int x = centerX - 210 + (col * 215);
            int y = startY + (row * 22);

            this.addRenderableWidget(Button.builder(
                    Component.literal((theme == selectedTheme ? "§6▶ " : "§f") + theme.getDisplayName()),
                    b -> {
                        this.selectedTheme = theme;
                        rebuildWidgets();
                    }
            ).bounds(x, y, 205, 20).build());
        }

        // Section Cooldown
        int cooldownY = startY + (7 * 22) + 8;

        // Bouton 2 Heures (Par défaut)
        this.addRenderableWidget(Button.builder(
                Component.literal((selectedCooldown == 7200L ? "§a✔ " : "") + "2 Heures (Standard)"),
                b -> {
                    this.selectedCooldown = 7200L;
                    rebuildWidgets();
                }
        ).bounds(centerX - 210, cooldownY, 135, 20).build());

        // Bouton 17 Ans (Légendaire / Mythique)
        this.addRenderableWidget(Button.builder(
                Component.literal((selectedCooldown >= 536112000L ? "§4✔ " : "") + "17 Ans (Mythique/Légendaire)"),
                b -> {
                    this.selectedCooldown = 536112000L; // 17 ans en secondes
                    rebuildWidgets();
                }
        ).bounds(centerX - 70, cooldownY, 170, 20).build());

        // Bouton 30 min / 1h / 24h
        this.addRenderableWidget(Button.builder(
                Component.literal("1 Heure"),
                b -> {
                    this.selectedCooldown = 3600L;
                    rebuildWidgets();
                }
        ).bounds(centerX + 105, cooldownY, 100, 20).build());

        // Boutons d'Action en bas
        int actionY = cooldownY + 28;

        // Bouton Sauvegarder
        this.addRenderableWidget(Button.builder(
                Component.literal("§a§lSauvegarder et Appliquer"),
                b -> {
                    PacketHandler.CHANNEL.sendToServer(new SaveConfigPacket(pos, selectedTheme.name(), selectedCooldown, false));
                    this.onClose();
                }
        ).bounds(centerX - 210, actionY, 200, 22).build());

        // Bouton Supprimer
        this.addRenderableWidget(Button.builder(
                Component.literal("§cSupprimer le Reloot"),
                b -> {
                    PacketHandler.CHANNEL.sendToServer(new SaveConfigPacket(pos, selectedTheme.name(), selectedCooldown, true));
                    this.onClose();
                }
        ).bounds(centerX + 5, actionY, 200, 22).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        int centerX = this.width / 2;

        graphics.drawCenteredString(this.font, "§6§lCONFIGURATION DU CONTENEUR §7(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")", centerX, 12, 0xFFFFFF);

        // Affichage du thème et délai sélectionné
        String cooldownStr = (selectedCooldown >= 536112000L) ? "§417 Ans" : "§b" + (selectedCooldown / 3600L) + "h " + ((selectedCooldown % 3600L) / 60L) + "m";
        graphics.drawString(this.font, "§7Thème: §e" + selectedTheme.getDisplayName() + " §8| §7Délai: " + cooldownStr, centerX - 210, 23, 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
