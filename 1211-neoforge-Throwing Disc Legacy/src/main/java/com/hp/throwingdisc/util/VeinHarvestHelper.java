package com.hp.throwingdisc.util;

import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import com.hp.throwingdisc.registry.ModEnchantments;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.slf4j.Logger;

public final class VeinHarvestHelper {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_SEARCH_SPAN = 16;
    private static final int MAX_PARTICLE_BLOCKS = 6;
    private static final ThreadLocal<Boolean> ACTIVE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private VeinHarvestHelper() {
    }

    public static boolean isActive() {
        return ACTIVE.get();
    }

    public static boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }

    public static boolean isSameOreType(BlockState origin, BlockState candidate) {
        if (candidate.isAir() || !isOre(candidate)) {
            return false;
        }
        if (candidate.is(origin.getBlock())) {
            return true;
        }
        return origin.getTags().anyMatch(tag -> {
            ResourceLocation id = tag.location();
            if ("c".equals(id.getNamespace()) && id.getPath().startsWith("ores/")) {
                return candidate.is(tag);
            }
            if ("minecraft".equals(id.getNamespace()) && id.getPath().endsWith("_ores")) {
                return candidate.is(tag);
            }
            return false;
        });
    }

    public static void onCreativeBreakEvent(BlockEvent.BreakEvent event) {
        if (ACTIVE.get() || !(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getPlayer() instanceof ServerPlayer serverPlayer) || !serverPlayer.isCreative()) {
            return;
        }
        ItemStack stack = serverPlayer.getMainHandItem();
        if (!(stack.getItem() instanceof ThrowingDiscItem)) {
            return;
        }
        harvestConnectedVeinFromHand(serverLevel, serverPlayer, stack, event.getPos(), event.getState());
    }

    public static void harvestConnectedVeinFromHand(
        ServerLevel serverLevel,
        ServerPlayer player,
        ItemStack stack,
        BlockPos originPos,
        BlockState originState
    ) {
        if (ACTIVE.get() || !isOre(originState)) {
            return;
        }

        int cleavingLevel = ModEnchantments.getLevel(serverLevel, stack, ModEnchantments.CLEAVING_HARVEST);
        int veinLevel = ModEnchantments.getLevel(serverLevel, stack, ModEnchantments.VEIN_HARVEST);
        if (cleavingLevel <= 0 || veinLevel <= 0) {
            return;
        }

        if (stack.getItem() instanceof TieredItem tieredItem && originState.is(tieredItem.getTier().getIncorrectBlocksForDrops())) {
            return;
        }

        int maxExtra = veinLevel * ThrowingDiscConfig.COMMON.veinHarvestBlocksPerLevel.get();
        if (maxExtra <= 0) {
            return;
        }

        float originHardness = Math.max(0.0F, originState.getDestroySpeed(serverLevel, originPos));
        double maxHardness = Math.max(
            originHardness + 2.0,
            ThrowingDiscConfig.COMMON.cleavingBaseHardness.get() + cleavingLevel * ThrowingDiscConfig.COMMON.cleavingHardnessPerLevel.get()
        );
        boolean creative = player.isCreative();

        ACTIVE.set(Boolean.TRUE);
        try {
            LongSet visited = new LongOpenHashSet();
            Queue<BlockPos> queue = new ArrayDeque<>();
            visited.add(originPos.asLong());
            queue.add(originPos.immutable());

            List<ItemStack> collectedDrops = new ArrayList<>();
            int collectedXp = 0;
            int brokenCount = 0;

            while (!queue.isEmpty() && brokenCount < maxExtra) {
                if (!creative && ThrowingDiscItem.isTooDamagedToThrow(stack)) {
                    break;
                }

                BlockPos current = queue.poll();
                for (int dx = -1; dx <= 1 && brokenCount < maxExtra; dx++) {
                    for (int dy = -1; dy <= 1 && brokenCount < maxExtra; dy++) {
                        for (int dz = -1; dz <= 1 && brokenCount < maxExtra; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) {
                                continue;
                            }
                            if (!creative && ThrowingDiscItem.isTooDamagedToThrow(stack)) {
                                break;
                            }

                            BlockPos nextPos = current.offset(dx, dy, dz);
                            if (Math.abs(nextPos.getX() - originPos.getX()) > MAX_SEARCH_SPAN
                                || Math.abs(nextPos.getY() - originPos.getY()) > MAX_SEARCH_SPAN
                                || Math.abs(nextPos.getZ() - originPos.getZ()) > MAX_SEARCH_SPAN) {
                                continue;
                            }
                            if (!visited.add(nextPos.asLong())) {
                                continue;
                            }

                            BlockState nextState = serverLevel.getBlockState(nextPos);
                            if (!isSameOreType(originState, nextState)) {
                                continue;
                            }

                            float hardness = nextState.getDestroySpeed(serverLevel, nextPos);
                            if (hardness < 0.0F || hardness > maxHardness) {
                                continue;
                            }
                            if (nextState.getBlock() instanceof GameMasterBlock) {
                                continue;
                            }
                            if (nextState.hasBlockEntity() && !ThrowingDiscConfig.COMMON.cleavingBreakBlockEntities.get()) {
                                continue;
                            }
                            if (stack.getItem() instanceof TieredItem tieredItem
                                && nextState.is(tieredItem.getTier().getIncorrectBlocksForDrops())) {
                                continue;
                            }
                            if (player.blockActionRestricted(serverLevel, nextPos, player.gameMode.getGameModeForPlayer())
                                || !serverLevel.mayInteract(player, nextPos)) {
                                continue;
                            }

                            BlockEvent.BreakEvent breakEvent = new BlockEvent.BreakEvent(serverLevel, nextPos, nextState, player);
                            NeoForge.EVENT_BUS.post(breakEvent);
                            if (breakEvent.isCanceled()) {
                                continue;
                            }

                            BlockEntity blockEntity = nextState.hasBlockEntity() ? serverLevel.getBlockEntity(nextPos) : null;
                            List<ItemStack> drops = creative
                                ? List.of()
                                : Block.getDrops(nextState, serverLevel, nextPos, blockEntity, player, stack);
                            int xp = creative
                                ? 0
                                : nextState.getExpDrop(serverLevel, nextPos, blockEntity, player, stack);

                            BlockState adjusted = nextState.getBlock().playerWillDestroy(serverLevel, nextPos, nextState, player);
                            if (brokenCount < MAX_PARTICLE_BLOCKS) {
                                serverLevel.levelEvent(2001, nextPos, Block.getId(nextState));
                            }
                            boolean removed = adjusted.onDestroyedByPlayer(
                                serverLevel,
                                nextPos,
                                player,
                                !creative,
                                serverLevel.getFluidState(nextPos)
                            );
                            if (!removed) {
                                continue;
                            }
                            adjusted.getBlock().destroy(serverLevel, nextPos, adjusted);
                            serverLevel.gameEvent(GameEvent.BLOCK_DESTROY, nextPos, GameEvent.Context.of(player, nextState));

                            if (!creative) {
                                for (ItemStack drop : drops) {
                                    mergeDrop(collectedDrops, drop);
                                }
                                collectedXp += xp;
                                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                            }

                            brokenCount++;
                            queue.add(nextPos.immutable());
                        }
                    }
                }
            }

            if (!creative) {
                Inventory inv = player.getInventory();
                for (ItemStack drop : collectedDrops) {
                    if (drop.isEmpty()) {
                        continue;
                    }
                    ItemStack copy = drop.copy();
                    inv.add(copy);
                    if (!copy.isEmpty()) {
                        ItemEntity entity = new ItemEntity(serverLevel, player.getX(), player.getY() + 0.5, player.getZ(), copy);
                        entity.setNoPickUpDelay();
                        entity.setTarget(player.getUUID());
                        serverLevel.addFreshEntity(entity);
                    }
                }
                if (collectedXp > 0) {
                    ExperienceOrb.award(serverLevel, player.position(), collectedXp);
                }
            }

            if (brokenCount > 0) {
                LOGGER.debug(
                    "Vein Harvest (melee) chained {} ore block(s) from {} for player {}",
                    brokenCount,
                    originPos,
                    player.getScoreboardName()
                );
            }
        } finally {
            ACTIVE.set(Boolean.FALSE);
        }
    }

    private static void mergeDrop(List<ItemStack> list, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remaining = stack.copy();
        for (ItemStack existing : list) {
            if (ItemStack.isSameItemSameComponents(existing, remaining) && existing.getCount() < existing.getMaxStackSize()) {
                int move = Math.min(existing.getMaxStackSize() - existing.getCount(), remaining.getCount());
                existing.grow(move);
                remaining.shrink(move);
                if (remaining.isEmpty()) {
                    return;
                }
            }
        }
        if (!remaining.isEmpty()) {
            list.add(remaining);
        }
    }
}
