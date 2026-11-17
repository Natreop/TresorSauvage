package fr.tresorssauvages;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = TresorsSauvages.MODID)
public final class TreasureEvents {
    private static final String TIER_KEY = "ts_tier";
    private static final String NEXT_RELOOT_KEY = "ts_next_reloot";

    private TreasureEvents() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !event.isNewChunk()) return;

        RandomSource random = level.random;
        if (random.nextInt(12) != 0) return;

        ChunkPos chunk = event.getChunk().getPos();
        int chestCount = 1 + (random.nextFloat() < 0.12f ? 1 : 0);

        for (int i = 0; i < chestCount; i++) {
            int x = chunk.getMinBlockX() + 2 + random.nextInt(12);
            int z = chunk.getMinBlockZ() + 2 + random.nextInt(12);
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos pos = new BlockPos(x, y, z);

            if (!level.getBlockState(pos).isAir()) continue;
            if (!level.getBlockState(pos.below()).isSolid()) continue;
            if (level.getFluidState(pos).isSource()) continue;

            level.setBlock(pos, Blocks.CHEST.defaultBlockState(), 3);
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ChestBlockEntity chest) {
                Tier tier = chooseTier(random);
                initializeChest(chest, tier, random, level.getGameTime());
            }
        }
    }

    @SubscribeEvent
    public static void onChestClick(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide || event.getHand() != InteractionHand.MAIN_HAND) return;

        BlockEntity blockEntity = level.getBlockEntity(event.getPos());
        if (!(blockEntity instanceof ChestBlockEntity chest)) return;

        var data = chest.getPersistentData();
        if (!data.contains(TIER_KEY) || !chest.isEmpty()) return;

        long nextTime = data.getLong(NEXT_RELOOT_KEY);
        if (level.getGameTime() < nextTime) return;

        RandomSource random = level.random;
        if (random.nextFloat() < 0.35f) {
            Tier tier = Tier.fromId(data.getInt(TIER_KEY));
            fillChest(chest, tier, random);
            chest.setCustomName(Component.literal(tier.displayName + " - Coffre"));
            chest.setChanged();
            data.putLong(NEXT_RELOOT_KEY, level.getGameTime() + 20L * 60L * 20L);
            Player player = event.getEntity();
            player.displayClientMessage(
                    Component.literal("§eCe coffre s'est régénéré !"),
                    true
            );
        } else {
            data.putLong(NEXT_RELOOT_KEY, level.getGameTime() + 20L * 60L * 5L);
            chest.setChanged();
        }
    }

    private static Tier chooseTier(RandomSource random) {
        int roll = random.nextInt(1000);
        if (roll < 5) return Tier.LEGENDARY;       // 0,5 %
        if (roll < 35) return Tier.EPIC;           // 3 %
        if (roll < 155) return Tier.RARE;          // 12 %
        return Tier.COMMON;                        // 84,5 %
    }

    private static void initializeChest(ChestBlockEntity chest, Tier tier, RandomSource random, long gameTime) {
        chest.getPersistentData().putInt(TIER_KEY, tier.id);
        chest.getPersistentData().putLong(NEXT_RELOOT_KEY, gameTime + 20L * 60L * 20L);
        chest.setCustomName(Component.literal(tier.displayName + " - Coffre"));
        fillChest(chest, tier, random);
        chest.setChanged();
    }

    private static void fillChest(ChestBlockEntity chest, Tier tier, RandomSource random) {
        List<Item> pool = tier.items;
        int rolls = tier.minRolls + random.nextInt(tier.maxRolls - tier.minRolls + 1);
        for (int i = 0; i < rolls; i++) {
            Item item = pool.get(random.nextInt(pool.size()));
            int amount = 1;
            if (item == Items.IRON_INGOT || item == Items.GOLD_INGOT || item == Items.DIAMOND
                    || item == Items.EMERALD || item == Items.LAPIS_LAZULI) {
                amount += random.nextInt(tier.maxStackBonus + 1);
            }
            int slot = random.nextInt(chest.getContainerSize());
            // On cherche une case vide pour éviter d'écraser un objet déjà tiré.
            for (int attempt = 0; attempt < chest.getContainerSize() && !chest.getItem(slot).isEmpty(); attempt++) {
                slot = (slot + 1) % chest.getContainerSize();
            }
            if (chest.getItem(slot).isEmpty()) chest.setItem(slot, new ItemStack(item, Math.min(amount, item.getMaxStackSize())));
        }
    }

    private enum Tier {
        COMMON(0, "Commun", 3, 6, 3,
                List.of(Items.BREAD, Items.APPLE, Items.COAL, Items.IRON_INGOT, Items.ARROW,
                        Items.LEATHER, Items.STRING, Items.TORCH, Items.COOKED_BEEF)),
        RARE(1, "Rare", 4, 7, 5,
                List.of(Items.IRON_INGOT, Items.GOLD_INGOT, Items.EMERALD, Items.GOLDEN_APPLE,
                        Items.ENDER_PEARL, Items.EXPERIENCE_BOTTLE, Items.DIAMOND, Items.CROSSBOW)),
        EPIC(2, "Épique", 5, 8, 8,
                List.of(Items.DIAMOND, Items.ENCHANTED_GOLDEN_APPLE, Items.NETHERITE_SCRAP,
                        Items.TOTEM_OF_UNDYING, Items.TRIDENT, Items.NETHERITE_INGOT,
                        Items.EXPERIENCE_BOTTLE, Items.SHULKER_SHELL)),
        LEGENDARY(3, "Légendaire", 6, 10, 12,
                List.of(Items.NETHERITE_INGOT, Items.ELYTRA, Items.DRAGON_EGG,
                        Items.NETHER_STAR, Items.BEACON, Items.ENCHANTED_GOLDEN_APPLE,
                        Items.TOTEM_OF_UNDYING, Items.TRIDENT));

        final int id;
        final String displayName;
        final int minRolls;
        final int maxRolls;
        final int maxStackBonus;
        final List<Item> items;

        Tier(int id, String displayName, int minRolls, int maxRolls, int maxStackBonus, List<Item> items) {
            this.id = id;
            this.displayName = displayName;
            this.minRolls = minRolls;
            this.maxRolls = maxRolls;
            this.maxStackBonus = maxStackBonus;
            this.items = items;
        }

        static Tier fromId(int id) {
            for (Tier tier : values()) if (tier.id == id) return tier;
            return COMMON;
        }
    }
}
