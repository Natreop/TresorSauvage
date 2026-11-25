package com.relootmod.event;

import com.relootmod.data.LootManager;
import com.relootmod.data.RelootBlockData;
import com.relootmod.data.RelootSavedData;
import com.relootmod.item.RelootWandItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ContainerInteractEvents {

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();

        // Si le joueur tient la baguette admin, l'item s'occupe d'ouvrir le menu
        if (player.getMainHandItem().getItem() instanceof RelootWandItem ||
            player.getOffhandItem().getItem() instanceof RelootWandItem) {
            return;
        }

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            RelootSavedData savedData = RelootSavedData.get(serverLevel);
            RelootBlockData blockData = savedData.getData(pos);

            if (blockData != null) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity == null) return;

                long nowEpoch = System.currentTimeMillis() / 1000L;
                long lastLoot = blockData.getLastLootTimeEpochSeconds();
                long cooldown = blockData.getCooldownSeconds();
                long elapsed = nowEpoch - lastLoot;

                if (lastLoot > 0 && elapsed < cooldown) {
                    long remaining = cooldown - elapsed;
                    String timeFormatted = formatTime(remaining);
                    player.sendSystemMessage(Component.literal(
                            "§c§l[Reloot] §cCe conteneur a déjà été pillé !\n" +
                            "§7Prochain reloot dans : §e" + timeFormatted
                    ));
                    level.playSound(null, pos, SoundEvents.CHEST_LOCKED, SoundSource.BLOCKS, 0.8f, 0.9f);
                } else {
                    // Le conteneur est prêt à être relooté !
                    LootManager.populateContainer(blockEntity, blockData.getTheme());
                    savedData.updateLastLoot(pos, nowEpoch);

                    player.sendSystemMessage(Component.literal(
                            "§a§l[Reloot] §aTu as fouillé une cache de type §e" + blockData.getTheme().getDisplayName() + " §a!"
                    ));
                    level.playSound(null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
    }

    private String formatTime(long seconds) {
        if (seconds >= 31536000L) { // Années
            long years = seconds / 31536000L;
            return years + " an(s)";
        }
        if (seconds >= 86400L) { // Jours
            long days = seconds / 86400L;
            long hours = (seconds % 86400L) / 3600L;
            return days + "j " + hours + "h";
        }
        if (seconds >= 3600L) { // Heures
            long hours = seconds / 3600L;
            long minutes = (seconds % 3600L) / 60L;
            return hours + "h " + minutes + "m";
        }
        if (seconds >= 60L) { // Minutes
            long minutes = seconds / 60L;
            long secs = seconds % 60L;
            return minutes + "m " + secs + "s";
        }
        return seconds + "s";
    }
}
