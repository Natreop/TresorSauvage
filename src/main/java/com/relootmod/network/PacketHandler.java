package com.relootmod.network;

import com.relootmod.RelootMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RelootMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(packetId++,
                OpenConfigPacket.class,
                OpenConfigPacket::encode,
                OpenConfigPacket::decode,
                OpenConfigPacket::handle
        );

        CHANNEL.registerMessage(packetId++,
                SaveConfigPacket.class,
                SaveConfigPacket::encode,
                SaveConfigPacket::decode,
                SaveConfigPacket::handle
        );
    }
}
