package com.relootmod.data;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class LootManager {
    private static final Map<LootTheme, Map<RarityTier, List<LootEntry>>> THEME_LOOTS = new EnumMap<>(LootTheme.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void initialize(File configFolder) {
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
        File configFile = new File(configFolder, "loot_tables.json");
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        }
        loadConfig(configFile);
    }

    private static void registerEntry(LootTheme theme, RarityTier rarity, String itemId, int min, int max, String name) {
        THEME_LOOTS.computeIfAbsent(theme, t -> new EnumMap<>(RarityTier.class))
                   .computeIfAbsent(rarity, r -> new ArrayList<>())
                   .add(new LootEntry(itemId, min, max, name, rarity));
    }

    public static void populateContainer(BlockEntity blockEntity, LootTheme theme) {
        RandomSource random = RandomSource.create();
        int itemsToSpawn = 2 + random.nextInt(5); // 2 à 6 items

        if (blockEntity instanceof Container container) {
            container.clearContent();
            int size = container.getContainerSize();
            if (size <= 0) return;

            Set<Integer> usedSlots = new HashSet<>();
            for (int i = 0; i < itemsToSpawn; i++) {
                int slot = random.nextInt(size);
                if (usedSlots.contains(slot)) continue;
                usedSlots.add(slot);

                ItemStack stack = generateRandomItem(theme, random);
                if (!stack.isEmpty()) {
                    container.setItem(slot, stack);
                }
            }
            container.setChanged();
        } else {
            blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                if (handler instanceof IItemHandlerModifiable modifiable) {
                    int size = modifiable.getSlots();
                    for (int i = 0; i < size; i++) {
                        modifiable.setStackInSlot(i, ItemStack.EMPTY);
                    }
                    Set<Integer> usedSlots = new HashSet<>();
                    for (int i = 0; i < itemsToSpawn; i++) {
                        int slot = random.nextInt(size);
                        if (usedSlots.contains(slot)) continue;
                        usedSlots.add(slot);

                        ItemStack stack = generateRandomItem(theme, random);
                        if (!stack.isEmpty()) {
                            modifiable.setStackInSlot(slot, stack);
                        }
                    }
                }
            });
        }
    }

    public static ItemStack generateRandomItem(LootTheme theme, RandomSource random) {
        RarityTier rarity = RarityTier.rollRarity(random);
        Map<RarityTier, List<LootEntry>> rarityMap = THEME_LOOTS.get(theme);

        if (rarityMap == null || rarityMap.isEmpty()) {
            return new ItemStack(Items.BREAD);
        }

        List<LootEntry> entries = rarityMap.get(rarity);
        if (entries == null || entries.isEmpty()) {
            // Repli sur une rareté inférieure si vide
            for (RarityTier lower : RarityTier.values()) {
                if (rarityMap.containsKey(lower) && !rarityMap.get(lower).isEmpty()) {
                    entries = rarityMap.get(lower);
                    rarity = lower;
                    break;
                }
            }
        }

        if (entries == null || entries.isEmpty()) {
            return new ItemStack(Items.BREAD);
        }

        LootEntry entry = entries.get(random.nextInt(entries.size()));
        ResourceLocation res = new ResourceLocation(entry.getItemId());
        Item item = BuiltInRegistries.ITEM.get(res);
        if (item == Items.AIR) {
            item = Items.BREAD;
        }

        int count = entry.getMinCount();
        if (entry.getMaxCount() > entry.getMinCount()) {
            count += random.nextInt(entry.getMaxCount() - entry.getMinCount() + 1);
        }

        ItemStack stack = new ItemStack(item, Math.max(1, count));
        applyRarityFormatting(stack, entry.getCustomName(), rarity);
        return stack;
    }

    public static void applyRarityFormatting(ItemStack stack, String customName, RarityTier rarity) {
        if (customName != null && !customName.isEmpty()) {
            stack.setHoverName(rarity.getFormattedName(customName));
        }

        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag display = tag.getCompound("display");
        ListTag loreTag = new ListTag();

        for (Component line : rarity.createLore()) {
            loreTag.add(StringTag.valueOf(Component.Serializer.toJson(line)));
        }

        display.put("Lore", loreTag);
        tag.put("display", display);
    }

    private static void loadConfig(File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            for (String themeName : json.keySet()) {
                LootTheme theme = LootTheme.fromString(themeName);
                JsonObject themeObj = json.getAsJsonObject(themeName);

                for (String rarityName : themeObj.keySet()) {
                    RarityTier rarity;
                    try {
                        rarity = RarityTier.valueOf(rarityName.toUpperCase(Locale.ROOT));
                    } catch (Exception e) {
                        continue;
                    }

                    JsonArray itemsArray = themeObj.getAsJsonArray(rarityName);
                    for (JsonElement elem : itemsArray) {
                        JsonObject itemObj = elem.getAsJsonObject();
                        String id = itemObj.get("id").getAsString();
                        int min = itemObj.has("min") ? itemObj.get("min").getAsInt() : 1;
                        int max = itemObj.has("max") ? itemObj.get("max").getAsInt() : 1;
                        String name = itemObj.has("name") ? itemObj.get("name").getAsString() : "";
                        registerEntry(theme, rarity, id, min, max, name);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createDefaultConfig(File file) {
        // Enregistrement des loots de base complets pour tous les thèmes
        // 1. Hôpital
        registerEntry(LootTheme.HOPITAL, RarityTier.COMMUN, "minecraft:paper", 1, 3, "Bandage Simple");
        registerEntry(LootTheme.HOPITAL, RarityTier.PEU_COMMUN, "minecraft:potion", 1, 1, "Antiseptique Médical");
        registerEntry(LootTheme.HOPITAL, RarityTier.RARE, "minecraft:glistering_melon_slice", 1, 2, "Trousse de Premiers Soins");
        registerEntry(LootTheme.HOPITAL, RarityTier.EPIQUE, "minecraft:golden_apple", 1, 1, "Kit Chirurgical Avancé");
        registerEntry(LootTheme.HOPITAL, RarityTier.LEGENDAIRE, "minecraft:enchanted_golden_apple", 1, 1, "Sérum Régénérant Militaire");
        registerEntry(LootTheme.HOPITAL, RarityTier.MYTHIQUE, "minecraft:totem_of_undying", 1, 1, "Défibrillateur Miraculeux");

        // 2. Police
        registerEntry(LootTheme.POLICE, RarityTier.COMMUN, "minecraft:stick", 1, 1, "Matraque en Bois");
        registerEntry(LootTheme.POLICE, RarityTier.PEU_COMMUN, "minecraft:chainmail_helmet", 1, 1, "Casque Anti-Émeute");
        registerEntry(LootTheme.POLICE, RarityTier.RARE, "minecraft:crossbow", 1, 1, "Taser Neutralisant");
        registerEntry(LootTheme.POLICE, RarityTier.EPIQUE, "minecraft:iron_chestplate", 1, 1, "Gilet Pare-Balles Police");
        registerEntry(LootTheme.POLICE, RarityTier.LEGENDAIRE, "minecraft:diamond_chestplate", 1, 1, "Gilet Tactique du SWAT");
        registerEntry(LootTheme.POLICE, RarityTier.MYTHIQUE, "minecraft:netherite_chestplate", 1, 1, "Blindage d'Intervention Lourde");

        // 3. Militaire
        registerEntry(LootTheme.MILITAIRE, RarityTier.COMMUN, "minecraft:iron_boots", 1, 1, "Rangers Militaires");
        registerEntry(LootTheme.MILITAIRE, RarityTier.PEU_COMMUN, "minecraft:tnt", 1, 2, "Charge C4");
        registerEntry(LootTheme.MILITAIRE, RarityTier.RARE, "minecraft:iron_sword", 1, 1, "Baïonnette Commando");
        registerEntry(LootTheme.MILITAIRE, RarityTier.EPIQUE, "minecraft:diamond_sword", 1, 1, "Lame Tactique des Forces Spéciales");
        registerEntry(LootTheme.MILITAIRE, RarityTier.LEGENDAIRE, "minecraft:netherite_helmet", 1, 1, "Casque Vision Thermique");
        registerEntry(LootTheme.MILITAIRE, RarityTier.MYTHIQUE, "minecraft:netherite_sword", 1, 1, "Prototype d'Exosquelette de Combat");

        // 4. Boîte à munitions
        registerEntry(LootTheme.MUNITIONS, RarityTier.COMMUN, "minecraft:arrow", 8, 24, "Balles 9mm");
        registerEntry(LootTheme.MUNITIONS, RarityTier.PEU_COMMUN, "minecraft:arrow", 16, 32, "Cartouches Calibre 12");
        registerEntry(LootTheme.MUNITIONS, RarityTier.RARE, "minecraft:iron_nugget", 10, 25, "Munitions 5.56mm OTAN");
        registerEntry(LootTheme.MUNITIONS, RarityTier.EPIQUE, "minecraft:gold_nugget", 6, 16, "Balles Perforantes .50 BMG");
        registerEntry(LootTheme.MUNITIONS, RarityTier.LEGENDAIRE, "minecraft:spectral_arrow", 16, 32, "Munitions Incendiaires Traçantes");
        registerEntry(LootTheme.MUNITIONS, RarityTier.MYTHIQUE, "minecraft:netherite_scrap", 1, 2, "Ogive Mini-Nucléaire");

        // 5. Ferme
        registerEntry(LootTheme.FERME, RarityTier.COMMUN, "minecraft:wheat", 4, 12, "Fagot de Paille Fraîche");
        registerEntry(LootTheme.FERME, RarityTier.PEU_COMMUN, "minecraft:carrot", 3, 8, "Carotte du Potager");
        registerEntry(LootTheme.FERME, RarityTier.RARE, "minecraft:iron_hoe", 1, 1, "Bêche de Fermier Renforcée");
        registerEntry(LootTheme.FERME, RarityTier.EPIQUE, "minecraft:golden_carrot", 2, 6, "Légume Hybride Fortifié");
        registerEntry(LootTheme.FERME, RarityTier.LEGENDAIRE, "minecraft:honey_bottle", 2, 4, "Miel Brut Bio de la Ruche");
        registerEntry(LootTheme.FERME, RarityTier.MYTHIQUE, "minecraft:diamond_hoe", 1, 1, "Faux Antique des Moissons");

        // 6. Cuisine
        registerEntry(LootTheme.CUISINE, RarityTier.COMMUN, "minecraft:bread", 2, 5, "Baguette Rassie");
        registerEntry(LootTheme.CUISINE, RarityTier.PEU_COMMUN, "minecraft:cooked_beef", 1, 3, "Plat de Bœuf Cuisiné");
        registerEntry(LootTheme.CUISINE, RarityTier.RARE, "minecraft:mushroom_stew", 1, 1, "Ragoût Forestier Chaud");
        registerEntry(LootTheme.CUISINE, RarityTier.EPIQUE, "minecraft:cooked_porkchop", 2, 4, "Ration Traiteur Sous-Vide");
        registerEntry(LootTheme.CUISINE, RarityTier.LEGENDAIRE, "minecraft:golden_apple", 1, 2, "Menu Gastronomique 3 Étoiles");
        registerEntry(LootTheme.CUISINE, RarityTier.MYTHIQUE, "minecraft:suspicious_stew", 1, 1, "Rocambolesque Festin Sacré");

        // 7. Survivant
        registerEntry(LootTheme.SURVIVANT, RarityTier.COMMUN, "minecraft:flint_and_steel", 1, 1, "Briquet Usagé");
        registerEntry(LootTheme.SURVIVANT, RarityTier.PEU_COMMUN, "minecraft:compass", 1, 1, "Boussole de Survie");
        registerEntry(LootTheme.SURVIVANT, RarityTier.RARE, "minecraft:leather_boots", 1, 1, "Bottes de Trekking Étanche");
        registerEntry(LootTheme.SURVIVANT, RarityTier.EPIQUE, "minecraft:fishing_rod", 1, 1, "Canne à Pêche en Carbone");
        registerEntry(LootTheme.SURVIVANT, RarityTier.LEGENDAIRE, "minecraft:clock", 1, 1, "Montre Militaire GPS");
        registerEntry(LootTheme.SURVIVANT, RarityTier.MYTHIQUE, "minecraft:elytra", 1, 1, "Parachute Tactique d'Évacuation");

        // 8. Gun
        registerEntry(LootTheme.GUN, RarityTier.COMMUN, "minecraft:wooden_shovel", 1, 1, "Pistolet Artisanal 9mm");
        registerEntry(LootTheme.GUN, RarityTier.PEU_COMMUN, "minecraft:crossbow", 1, 1, "Pistolet Automatique Compact");
        registerEntry(LootTheme.GUN, RarityTier.RARE, "minecraft:bow", 1, 1, "Fusil à Pompe Calibre 12");
        registerEntry(LootTheme.GUN, RarityTier.EPIQUE, "minecraft:crossbow", 1, 1, "Fusil d'Assaut Militaire");
        registerEntry(LootTheme.GUN, RarityTier.LEGENDAIRE, "minecraft:bow", 1, 1, "Fusil Sniper de Haute Précision");
        registerEntry(LootTheme.GUN, RarityTier.MYTHIQUE, "minecraft:netherite_hoe", 1, 1, "Canon Lourd Automatique");

        // 9. NOUVEAU : Magasin de nourriture (Supermarché / Épicerie)
        registerEntry(LootTheme.MAGASIN_NOURRITURE, RarityTier.COMMUN, "minecraft:cookie", 4, 10, "Paquet de Biscuits Secs");
        registerEntry(LootTheme.MAGASIN_NOURRITURE, RarityTier.PEU_COMMUN, "minecraft:apple", 2, 6, "Pomme d'Étalage");
        registerEntry(LootTheme.MAGASIN_NOURRITURE, RarityTier.RARE, "minecraft:baked_potato", 2, 5, "Boîte de Chips Barbecue");
        registerEntry(LootTheme.MAGASIN_NOURRITURE, RarityTier.EPIQUE, "minecraft:cake", 1, 1, "Gâteau d'Anniversaire Sous Boîte");
        registerEntry(LootTheme.MAGASIN_NOURRITURE, RarityTier.LEGENDAIRE, "minecraft:potion", 1, 2, "Pack de Canettes Énergisantes");
        registerEntry(LootTheme.MAGASIN_NOURRITURE, RarityTier.MYTHIQUE, "minecraft:golden_apple", 2, 4, "Réserve VIP de Chocolat Précieux");

        // 10. NOUVEAU : Bricolage / Quincaillerie
        registerEntry(LootTheme.BRICOLAGE, RarityTier.COMMUN, "minecraft:iron_nugget", 4, 16, "Lot de Vis et Clous");
        registerEntry(LootTheme.BRICOLAGE, RarityTier.PEU_COMMUN, "minecraft:string", 2, 8, "Ruban Adhésif Blindé");
        registerEntry(LootTheme.BRICOLAGE, RarityTier.RARE, "minecraft:iron_pickaxe", 1, 1, "Marteau Perforateur");
        registerEntry(LootTheme.BRICOLAGE, RarityTier.EPIQUE, "minecraft:anvil", 1, 1, "Établi de Chaudronnier");
        registerEntry(LootTheme.BRICOLAGE, RarityTier.LEGENDAIRE, "minecraft:diamond_pickaxe", 1, 1, "Meuleuse Industrielle");
        registerEntry(LootTheme.BRICOLAGE, RarityTier.MYTHIQUE, "minecraft:netherite_pickaxe", 1, 1, "Découpeur Plasma Haute Pression");

        // 11. NOUVEAU : Station-service / Garage
        registerEntry(LootTheme.STATION_SERVICE, RarityTier.COMMUN, "minecraft:coal", 2, 8, "Bidon d'Huile Usagée");
        registerEntry(LootTheme.STATION_SERVICE, RarityTier.PEU_COMMUN, "minecraft:copper_ingot", 2, 6, "Câbles de Démarrage en Cuivre");
        registerEntry(LootTheme.STATION_SERVICE, RarityTier.RARE, "minecraft:shears", 1, 1, "Pince Monseigneur Professionnelle");
        registerEntry(LootTheme.STATION_SERVICE, RarityTier.EPIQUE, "minecraft:redstone_block", 1, 2, "Batterie de Camion 24V");
        registerEntry(LootTheme.STATION_SERVICE, RarityTier.LEGENDAIRE, "minecraft:lava_bucket", 1, 1, "Jerrican d'Essence Sans-Plomb 98");
        registerEntry(LootTheme.STATION_SERVICE, RarityTier.MYTHIQUE, "minecraft:beacon", 1, 1, "Moteur V8 Turbo Modifié");

        // 12. NOUVEAU : Pharmacie / Labo
        registerEntry(LootTheme.PHARMACIE, RarityTier.COMMUN, "minecraft:glass_bottle", 2, 6, "Flacon d'Alcool à 90°");
        registerEntry(LootTheme.PHARMACIE, RarityTier.PEU_COMMUN, "minecraft:spider_eye", 1, 3, "Échantillon de Toxine");
        registerEntry(LootTheme.PHARMACIE, RarityTier.RARE, "minecraft:glowstone_dust", 2, 5, "Antibiotiques à Large Spectre");
        registerEntry(LootTheme.PHARMACIE, RarityTier.EPIQUE, "minecraft:blaze_powder", 1, 3, "Stéroïdes Anabolisants");
        registerEntry(LootTheme.PHARMACIE, RarityTier.LEGENDAIRE, "minecraft:dragon_breath", 1, 1, "Antidote à la Peste Zombie");
        registerEntry(LootTheme.PHARMACIE, RarityTier.MYTHIQUE, "minecraft:nether_star", 1, 1, "Molécule Génétique Synthétique");

        // 13. NOUVEAU : Chasse & Pêche
        registerEntry(LootTheme.CHASSE_PECHE, RarityTier.COMMUN, "minecraft:feather", 4, 12, "Plumes d'Appeau");
        registerEntry(LootTheme.CHASSE_PECHE, RarityTier.PEU_COMMUN, "minecraft:leather", 2, 6, "Peau de Cerf Tanné");
        registerEntry(LootTheme.CHASSE_PECHE, RarityTier.RARE, "minecraft:lead", 1, 2, "Piège à Loup Renforcé");
        registerEntry(LootTheme.CHASSE_PECHE, RarityTier.EPIQUE, "minecraft:crossbow", 1, 1, "Arbalète de Chasseur Poulie");
        registerEntry(LootTheme.CHASSE_PECHE, RarityTier.LEGENDAIRE, "minecraft:trident", 1, 1, "Harpon en Titane");
        registerEntry(LootTheme.CHASSE_PECHE, RarityTier.MYTHIQUE, "minecraft:bow", 1, 1, "Arc Long Ancestral du Grand Traqueur");

        // 14. NOUVEAU : Banque / Coffre-fort
        registerEntry(LootTheme.BANQUE, RarityTier.COMMUN, "minecraft:paper", 5, 20, "Liasses de Billets Dévalués");
        registerEntry(LootTheme.BANQUE, RarityTier.PEU_COMMUN, "minecraft:gold_nugget", 8, 24, "Pièces d'Or de Collection");
        registerEntry(LootTheme.BANQUE, RarityTier.RARE, "minecraft:gold_ingot", 2, 6, "Lingot d'Or Pur 24 Carats");
        registerEntry(LootTheme.BANQUE, RarityTier.EPIQUE, "minecraft:diamond", 1, 3, "Diamant Brut d'Anvers");
        registerEntry(LootTheme.BANQUE, RarityTier.LEGENDAIRE, "minecraft:emerald_block", 1, 2, "Émeraude Royale d'Empire");
        registerEntry(LootTheme.BANQUE, RarityTier.MYTHIQUE, "minecraft:netherite_ingot", 1, 2, "Lingot de Netherite Forgé");

        // Sauvegarde au format JSON
        JsonObject root = new JsonObject();
        for (Map.Entry<LootTheme, Map<RarityTier, List<LootEntry>>> themeEntry : THEME_LOOTS.entrySet()) {
            JsonObject themeObj = new JsonObject();
            for (Map.Entry<RarityTier, List<LootEntry>> rarityEntry : themeEntry.getValue().entrySet()) {
                JsonArray array = new JsonArray();
                for (LootEntry entry : rarityEntry.getValue()) {
                    JsonObject itemObj = new JsonObject();
                    itemObj.addProperty("id", entry.getItemId());
                    itemObj.addProperty("min", entry.getMinCount());
                    itemObj.addProperty("max", entry.getMaxCount());
                    itemObj.addProperty("name", entry.getCustomName());
                    array.add(itemObj);
                }
                themeObj.add(rarityEntry.getKey().name().toLowerCase(Locale.ROOT), array);
            }
            root.add(themeEntry.getKey().name(), themeObj);
        }

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
