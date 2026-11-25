package com.relootmod.network;

import com.relootmod.data.LootTheme;
import com.relootmod.data.RelootSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SaveConfigPacket {
    private final BlockPos pos;
    private final String themeName;
    private final long cooldownSeconds;
    private final boolean delete;

    public SaveConfigPacket(BlockPos pos, String themeName, long cooldownSeconds, boolean delete) {
        this.pos = pos;
        this.themeName = themeName;
        this.cooldownSeconds = cooldownSeconds;
        this.delete = delete;
    }

    public static void encode(SaveConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.themeName);
        buf.writeLong(msg.cooldownSeconds);
        buf.writeBoolean(msg.delete);
    }

    public static SaveConfigPacket decode(FriendlyByteBuf buf) {
        return new SaveConfigPacket(
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readLong(),
                buf.readBoolean()
        );
    }

    public static void handle(SaveConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.hasPermissions(2)) { // Nécessite niveau permission OP ou creative
                ServerLevel level = player.serverLevel();
                RelootSavedData savedData = RelootSavedData.get(level);

                if (msg.delete) {
                    savedData.removeBlock(msg.pos);
                    player.sendSystemMessage(Component.literal("§c[RelootMod] Configuration de reloot supprimée pour ce bloc."));
                } else {
                    LootTheme theme = LootTheme.fromString(msg.themeName);
                    savedData.setBlock(msg.pos, theme, msg.cooldownSeconds);

                    String cooldownDisplay;
                    if (msg.cooldownSeconds >= 536112000L) { // ~17 ans
                        cooldownDisplay = "17 ans (Mythique / Légendaire)";
                    } else if (msg.cooldownSeconds >= 3600L) {
                        cooldownDisplay = (msg.cooldownSeconds / 3600L) + " heure(s)";
                    } else {
                        cooldownDisplay = (msg.cooldownSeconds / 60L) + " minute(s)";
                    }

                    player.sendSystemMessage(Component.literal(
                            "§a[RelootMod] Bloc configuré avec succès !\n" +
                            "§7Thème: §e" + theme.getDisplayName() + "\n" +
                            "§7Temps de reloot: §b" + cooldownDisplay
                    ));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
