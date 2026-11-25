package com.relootmod.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class RelootSavedData extends SavedData {
    private static final String DATA_NAME = "relootmod_containers";
    private final Map<BlockPos, RelootBlockData> blocks = new HashMap<>();

    public RelootSavedData() {}

    public static RelootSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(RelootSavedData::load, RelootSavedData::new, DATA_NAME);
    }

    public RelootBlockData getData(BlockPos pos) {
        return blocks.get(pos);
    }

    public void setBlock(BlockPos pos, LootTheme theme, long cooldownSeconds) {
        RelootBlockData existing = blocks.get(pos);
        if (existing == null) {
            blocks.put(pos, new RelootBlockData(theme, cooldownSeconds, 0L));
        } else {
            existing.setTheme(theme);
            existing.setCooldownSeconds(cooldownSeconds);
        }
        setDirty();
    }

    public void updateLastLoot(BlockPos pos, long epochSeconds) {
        RelootBlockData data = blocks.get(pos);
        if (data != null) {
            data.setLastLootTimeEpochSeconds(epochSeconds);
            setDirty();
        }
    }

    public void removeBlock(BlockPos pos) {
        if (blocks.remove(pos) != null) {
            setDirty();
        }
    }

    public static RelootSavedData load(CompoundTag tag) {
        RelootSavedData data = new RelootSavedData();
        ListTag list = tag.getList("Blocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag blockTag = list.getCompound(i);
            BlockPos pos = new BlockPos(blockTag.getInt("X"), blockTag.getInt("Y"), blockTag.getInt("Z"));
            RelootBlockData blockData = RelootBlockData.load(blockTag.getCompound("Data"));
            data.blocks.put(pos, blockData);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, RelootBlockData> entry : blocks.entrySet()) {
            CompoundTag blockTag = new CompoundTag();
            blockTag.putInt("X", entry.getKey().getX());
            blockTag.putInt("Y", entry.getKey().getY());
            blockTag.putInt("Z", entry.getKey().getZ());
            blockTag.put("Data", entry.getValue().save());
            list.add(blockTag);
        }
        tag.put("Blocks", list);
        return tag;
    }
}
