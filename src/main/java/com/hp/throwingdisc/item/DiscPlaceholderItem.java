package com.hp.throwingdisc.item;

import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.entity.ThrownDiscEntity;
import com.hp.throwingdisc.registry.ModItems;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class DiscPlaceholderItem extends Item {
    public static final String TAG_DISC_UUID = "DiscUuid";
    public static final String TAG_DISC_ITEM = "DiscItem";
    public static final String TAG_SPAWN_TIME = "SpawnGameTime";

    public DiscPlaceholderItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack createFor(ThrownDiscEntity discEntity, ItemStack originalDisc, long gameTime) {
        ItemStack placeholder = new ItemStack(ModItems.DISC_PLACEHOLDER.get());
        CompoundTag tag = placeholder.getOrCreateTag();
        tag.putUUID(TAG_DISC_UUID, discEntity.getUUID());
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(originalDisc.getItem());
        if (key != null) {
            tag.putString(TAG_DISC_ITEM, key.toString());
        }
        tag.putLong(TAG_SPAWN_TIME, gameTime);
        return placeholder;
    }

    public static ItemStack createClientPreview(ItemStack originalDisc) {
        ItemStack placeholder = new ItemStack(ModItems.DISC_PLACEHOLDER.get());
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(originalDisc.getItem());
        if (key != null) {
            placeholder.getOrCreateTag().putString(TAG_DISC_ITEM, key.toString());
        }
        return placeholder;
    }

    @Nullable
    public static UUID getDiscUuid(ItemStack stack) {
        if (!stack.is(ModItems.DISC_PLACEHOLDER.get())) {
            return null;
        }
        CompoundTag tag = stack.getTag();
        return tag != null && tag.hasUUID(TAG_DISC_UUID) ? tag.getUUID(TAG_DISC_UUID) : null;
    }

    public static boolean matches(ItemStack stack, UUID discUuid) {
        return discUuid.equals(getDiscUuid(stack));
    }

    public static void removeMatchingPlaceholders(Player player, UUID discUuid) {
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.items.size(); i++) {
            if (matches(inv.items.get(i), discUuid)) {
                inv.items.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < inv.offhand.size(); i++) {
            if (matches(inv.offhand.get(i), discUuid)) {
                inv.offhand.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < inv.armor.size(); i++) {
            if (matches(inv.armor.get(i), discUuid)) {
                inv.armor.set(i, ItemStack.EMPTY);
            }
        }
        if (player.containerMenu != null && matches(player.containerMenu.getCarried(), discUuid)) {
            player.containerMenu.setCarried(ItemStack.EMPTY);
        }
    }

    public static void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) {
            return;
        }
        Inventory playerInv = player.getInventory();
        AbstractContainerMenu menu = event.getContainer();
        for (Slot slot : menu.slots) {
            if (slot.container != playerInv && slot.getItem().is(ModItems.DISC_PLACEHOLDER.get())) {
                slot.set(ItemStack.EMPTY);
            }
        }
        if (menu.getCarried().is(ModItems.DISC_PLACEHOLDER.get())) {
            menu.setCarried(ItemStack.EMPTY);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(entity instanceof Player player)) {
            stack.setCount(0);
            return;
        }
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID(TAG_DISC_UUID)) {
            stack.setCount(0);
            return;
        }
        long spawnTime = tag.getLong(TAG_SPAWN_TIME);
        long age = serverLevel.getGameTime() - spawnTime;
        if (age >= 0 && age < 2) {
            return;
        }
        int maxLifetime = ThrowingDiscConfig.COMMON.maxReturnTicks.get() + 300;
        Entity found = serverLevel.getEntity(tag.getUUID(TAG_DISC_UUID));
        if (!(found instanceof ThrownDiscEntity thrownDisc)
            || !thrownDisc.isAlive()
            || thrownDisc.getOwner() != player
            || age > maxLifetime) {
            stack.setCount(0);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        return false;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        entity.discard();
        return true;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (slotChanged) {
            return true;
        }
        return !newStack.is(this);
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_DISC_ITEM, Tag.TAG_STRING)) {
            ResourceLocation id = ResourceLocation.tryParse(tag.getString(TAG_DISC_ITEM));
            if (id != null && ForgeRegistries.ITEMS.containsKey(id)) {
                Item discItem = ForgeRegistries.ITEMS.getValue(id);
                if (discItem != null && discItem != this) {
                    return Component.translatable("item.throwingdisc.disc_placeholder.named", discItem.getDescription())
                        .withStyle(ChatFormatting.GRAY);
                }
            }
        }
        return Component.translatable(this.getDescriptionId(stack)).withStyle(ChatFormatting.GRAY);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.throwingdisc.disc_placeholder").withStyle(ChatFormatting.DARK_GRAY));
    }
}
