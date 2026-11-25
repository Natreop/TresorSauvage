package com.relootmod;

import com.relootmod.data.LootManager;
import com.relootmod.event.ContainerInteractEvents;
import com.relootmod.init.ModItems;
import com.relootmod.network.PacketHandler;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;

@Mod(RelootMod.MOD_ID)
public class RelootMod {
    public static final String MOD_ID = "relootmod";

    public RelootMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        MinecraftForge.EVENT_BUS.register(new ContainerInteractEvents());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            PacketHandler.register();
            File configFolder = new File(FMLPaths.CONFIGDIR.get().toFile(), MOD_ID);
            LootManager.initialize(configFolder);
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES ||
            event.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
            event.accept(ModItems.RELOOT_WAND);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        File configFolder = new File(FMLPaths.CONFIGDIR.get().toFile(), MOD_ID);
        LootManager.initialize(configFolder);
    }
}
