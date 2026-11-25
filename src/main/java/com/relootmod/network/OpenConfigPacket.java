package com.relootmod.network;

import com.relootmod.client.RelootConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class OpenConfigPacket {
    private final BlockPos pos;
    private final String currentTheme;
    private final long currentCooldown;
    private final boolean isConfigured;

    public OpenConfigPacket(BlockPos pos, String currentTheme, long currentCooldown, boolean isConfigured) {
        this.pos = pos;
        this.currentTheme = currentTheme;
        this.currentCooldown = currentCooldown;
        this.isConfigured = isConfigured;
    }

    public static void encode(OpenConfigPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.currentTheme);
        buf.writeLong(msg.currentCooldown);
        buf.writeBoolean(msg.isConfigured);
    }

    public static OpenConfigPacket decode(FriendlyByteBuf buf) {
        return new OpenConfigPacket(
                buf.readBlockPos(),
                buf.readUtf(),
                buf.readLong(),
                buf.readBoolean()
        );
    }

    public static void handle(OpenConfigPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft.getInstance().setScreen(
                        new RelootConfigScreen(msg.pos, msg.currentTheme, msg.currentCooldown, msg.isConfigured)
                );
            });
        });
        ctx.get().setPacketHandled(true);
    }
}
