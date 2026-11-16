package com.hp.throwingdisc.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hp.throwingdisc.config.DiscDefaults;
import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.entity.ThrownDiscEntity;
import com.hp.throwingdisc.registry.ModEnchantments;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ThrowingDiscItem extends TieredItem {
    private static final DecimalFormat STAT_FORMAT = new DecimalFormat("0.#");
    private final DiscDefaults defaults;

    public ThrowingDiscItem(DiscDefaults defaults, Item.Properties properties) {
        super(defaults.baseTier(), applyBaseProperties(defaults, properties));
        this.defaults = defaults;
    }

    private static Item.Properties applyBaseProperties(DiscDefaults defaults, Item.Properties properties) {
        Item.Properties configured = properties.stacksTo(1).durability(defaults.durability());
        if (defaults.fireResistant()) {
            configured = configured.fireResistant();
        }
        return configured;
    }

    public DiscDefaults defaults() {
        return this.defaults;
    }

    public static boolean isTooDamagedToThrow(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? ThrowingDiscConfig.COMMON.stats(this.defaults).durability.get()
            : this.defaults.durability();
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            double attackDamage = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
                ? ThrowingDiscConfig.COMMON.stats(this.defaults).meleeDamageBonus.get()
                : this.defaults.meleeDamageBonus();
            double attackSpeed = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
                ? ThrowingDiscConfig.COMMON.stats(this.defaults).attackSpeedBonus.get()
                : this.defaults.attackSpeedBonus();

            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.put(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Weapon modifier", attackDamage, AttributeModifier.Operation.ADDITION)
            );
            builder.put(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Weapon modifier", attackSpeed, AttributeModifier.Operation.ADDITION)
            );
            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND && !ThrowingDiscConfig.COMMON.allowOffhandThrow.get()) {
            return InteractionResultHolder.pass(stack);
        }

        if (isTooDamagedToThrow(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        ThrowingDiscConfig.FlightStats flight = ThrowingDiscConfig.COMMON.flight(this.defaults);
        int ricochetLevel = ModEnchantments.getLevel(stack, ModEnchantments.RICOCHET);
        int tailwindLevel = ModEnchantments.getLevel(stack, ModEnchantments.TAILWIND);

        float baseDamage = flight.thrownDamage.get().floatValue();
        double speed = flight.throwSpeed.get() * (1.0 + tailwindLevel * ThrowingDiscConfig.COMMON.tailwindSpeedBonusPerLevel.get());
        double maxRange = flight.maxRange.get() + ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraRangePerLevel.get();
        int maxBounces = flight.maxBounces.get() + ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraBouncesPerLevel.get();
        int cooldown = flight.cooldownTicks.get();

        ItemStack resultStack = stack;
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ItemStack thrownStack = stack.copyWithCount(1);
            if (!player.getAbilities().instabuild) {
                ServerPlayer serverPlayer = player instanceof ServerPlayer sp ? sp : null;
                thrownStack.hurt(1, serverLevel.getRandom(), serverPlayer);
            }

            int originSlot = hand == InteractionHand.OFF_HAND ? ThrownDiscEntity.SLOT_OFFHAND : player.getInventory().selected;
            ThrownDiscEntity discEntity = new ThrownDiscEntity(level, player, thrownStack, originSlot);
            discEntity.configureFlight(baseDamage, speed, maxRange, maxBounces, player.getAbilities().instabuild);
            discEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float) speed, 0.15F);
            level.addFreshEntity(discEntity);

            level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.TRIDENT_THROW,
                SoundSource.PLAYERS,
                0.85F,
                1.35F
            );

            if (!player.getAbilities().instabuild) {
                if (ThrowingDiscConfig.COMMON.useSlotPlaceholder.get()) {
                    resultStack = DiscPlaceholderItem.createFor(discEntity, stack, level.getGameTime());
                    player.setItemInHand(hand, resultStack);
                } else {
                    stack.shrink(1);
                    resultStack = stack;
                }
            }
        } else if (!player.getAbilities().instabuild && ThrowingDiscConfig.COMMON.useSlotPlaceholder.get()) {
            resultStack = DiscPlaceholderItem.createClientPreview(stack);
        }

        if (cooldown > 0) {
            player.getCooldowns().addCooldown(this, cooldown);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(resultStack, level.isClientSide());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, p -> p.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (enchantment == Enchantments.SWEEPING_EDGE) {
            return false;
        }
        if (enchantment == Enchantments.BLOCK_FORTUNE || enchantment == Enchantments.SILK_TOUCH) {
            return ModEnchantments.getLevel(stack, ModEnchantments.CLEAVING_HARVEST) > 0;
        }
        if (enchantment == Enchantments.SHARPNESS
            || enchantment == Enchantments.SMITE
            || enchantment == Enchantments.BANE_OF_ARTHROPODS
            || enchantment == Enchantments.FIRE_ASPECT
            || enchantment == Enchantments.MOB_LOOTING
            || enchantment == Enchantments.UNBREAKING
            || enchantment == Enchantments.MENDING
            || enchantment == Enchantments.VANISHING_CURSE) {
            return true;
        }
        return super.canApplyAtEnchantingTable(stack, enchantment);
    }

    public Projectile createProjectile(Level level, Position pos, ItemStack stack) {
        ThrowingDiscConfig.FlightStats flight = ThrowingDiscConfig.COMMON.flight(this.defaults);
        int ricochetLevel = ModEnchantments.getLevel(stack, ModEnchantments.RICOCHET);
        int tailwindLevel = ModEnchantments.getLevel(stack, ModEnchantments.TAILWIND);

        float baseDamage = flight.thrownDamage.get().floatValue();
        double speed = flight.throwSpeed.get() * (1.0 + tailwindLevel * ThrowingDiscConfig.COMMON.tailwindSpeedBonusPerLevel.get());
        double maxRange = flight.maxRange.get() + ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraRangePerLevel.get();
        int maxBounces = flight.maxBounces.get() + ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraBouncesPerLevel.get();

        ItemStack copy = stack.copyWithCount(1);
        if (level instanceof ServerLevel serverLevel && !isTooDamagedToThrow(copy)) {
            copy.hurt(1, serverLevel.getRandom(), null);
        }
        ThrownDiscEntity entity = new ThrownDiscEntity(level, pos.x(), pos.y(), pos.z(), copy);
        entity.configureFlight(baseDamage, speed, maxRange, maxBounces, false);
        return entity;
    }

    public float dispensePower() {
        return ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? ThrowingDiscConfig.COMMON.flight(this.defaults).throwSpeed.get().floatValue()
            : (float) this.defaults.throwSpeed();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        double damage = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? ThrowingDiscConfig.COMMON.flight(this.defaults).thrownDamage.get()
            : this.defaults.thrownDamage();
        double range = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? ThrowingDiscConfig.COMMON.flight(this.defaults).maxRange.get()
            : this.defaults.maxRange();
        int bounces = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? ThrowingDiscConfig.COMMON.flight(this.defaults).maxBounces.get()
            : this.defaults.maxBounces();

        if (ThrowingDiscConfig.COMMON_SPEC.isLoaded()) {
            int ricochetLevel = ModEnchantments.getLevel(stack, ModEnchantments.RICOCHET);
            if (ricochetLevel > 0) {
                range += ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraRangePerLevel.get();
                bounces += ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraBouncesPerLevel.get();
            }
        }

        tooltip.add(Component.translatable("tooltip.throwingdisc.thrown_damage", STAT_FORMAT.format(damage)).withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("tooltip.throwingdisc.max_range", STAT_FORMAT.format(range)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.throwingdisc.max_bounces", bounces).withStyle(ChatFormatting.GRAY));

        if (isTooDamagedToThrow(stack)) {
            tooltip.add(Component.translatable("tooltip.throwingdisc.too_damaged").withStyle(ChatFormatting.RED));
        }
    }
}
