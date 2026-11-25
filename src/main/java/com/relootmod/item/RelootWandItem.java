package com.relootmod.item;

import com.relootmod.data.RelootBlockData;
import com.relootmod.data.RelootSavedData;
import com.relootmod.network.OpenConfigPacket;
import com.relootmod.network.PacketHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class RelootWandItem extends Item {
    public RelootWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
                    RelootSavedData savedData = RelootSavedData.get((ServerLevel) level);
                    RelootBlockData blockData = savedData.getData(pos);

                    String currentTheme = (blockData != null) ? blockData.getTheme().name() : "MAGASIN_NOURRITURE";
                    long currentCooldown = (blockData != null) ? blockData.getCooldownSeconds() : 7200L;
                    boolean isConfigured = (blockData != null);

                    PacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer),
                            new OpenConfigPacket(pos, currentTheme, currentCooldown, isConfigured));
                    return InteractionResult.SUCCESS;
                }
            } else {
                if (context.getPlayer() != null) {
                    context.getPlayer().sendSystemMessage(
                            Component.literal("§cCe bloc ne possède pas d'inventaire ou ne peut pas être relooté.")
                    );
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§6Outil d'administration de reloot"));
        tooltip.add(Component.literal("§7Fais un §eClic Droit §7sur n'importe quel conteneur"));
        tooltip.add(Component.literal("§7(coffre, baril, etc.) pour ouvrir son menu"));
        tooltip.add(Component.literal("§7et régler son thème, son temps et sa rareté."));
    }
}
