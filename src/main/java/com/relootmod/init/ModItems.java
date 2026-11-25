package com.relootmod.init;

import com.relootmod.RelootMod;
import com.relootmod.item.RelootWandItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RelootMod.MOD_ID);

    public static final RegistryObject<Item> RELOOT_WAND = ITEMS.register("reloot_wand",
            () -> new RelootWandItem(new Item.Properties().stacksTo(1)));
}
