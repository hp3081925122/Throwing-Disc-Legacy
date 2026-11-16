package com.hp.throwingdisc.item;

import com.hp.throwingdisc.config.DiscDefaults;
import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.entity.ThrownDiscEntity;
import com.hp.throwingdisc.registry.ModEnchantments;
import com.hp.throwingdisc.util.VeinHarvestHelper;
import java.text.DecimalFormat;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ThrowingDiscItem extends TieredItem implements ProjectileItem {
    private static final DecimalFormat STAT_FORMAT = new DecimalFormat("0.#");
    private final DiscDefaults defaults;

    public ThrowingDiscItem(DiscDefaults defaults, Item.Properties properties) {
        this(
            defaults,
            ThrowingDiscConfig.STARTUP.stats(defaults),
            properties
        );
    }

    private ThrowingDiscItem(DiscDefaults defaults, ThrowingDiscConfig.StartupStats startupStats, Item.Properties properties) {
        super(
            new ConfiguredDiscTier(
                defaults.baseTier(),
                startupStats.durability.get(),
                startupStats.meleeDamageBonus.get().floatValue()
            ),
            applyProperties(defaults, startupStats, properties)
        );
        this.defaults = defaults;
    }

    private static Item.Properties applyProperties(
        DiscDefaults defaults,
        ThrowingDiscConfig.StartupStats startupStats,
        Item.Properties properties
    ) {
        Item.Properties configured = properties
            .stacksTo(1)
            .attributes(createAttributes(startupStats.meleeDamageBonus.get(), startupStats.attackSpeedBonus.get()));
        if (defaults.fireResistant()) {
            configured = configured.fireResistant();
        }
        return configured;
    }

    public static ItemAttributeModifiers createAttributes(double attackDamageBonus, double attackSpeedBonus) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(BASE_ATTACK_DAMAGE_ID, attackDamageBonus, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .add(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeedBonus, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND
            )
            .build();
    }

    public DiscDefaults defaults() {
        return this.defaults;
    }

    public static boolean isTooDamagedToThrow(ItemStack stack) {
        return stack.isDamageableItem() && stack.getDamageValue() >= stack.getMaxDamage() - 1;
    }

    public static boolean hasCleavingHarvest(ItemStack stack) {
        return !stack.isEmpty() && stack.getTagEnchantments().keySet().stream().anyMatch(h -> h.is(ModEnchantments.CLEAVING_HARVEST));
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !player.isCreative() || hasCleavingHarvest(player.getMainHandItem());
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (hasCleavingHarvest(stack) && !isTooDamagedToThrow(stack) && !state.is(this.getTier().getIncorrectBlocksForDrops())) {
            return this.getTier().getSpeed();
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (hasCleavingHarvest(stack) && !isTooDamagedToThrow(stack)) {
            return !state.is(this.getTier().getIncorrectBlocksForDrops());
        }
        return super.isCorrectToolForDrops(stack, state);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (hasCleavingHarvest(stack)) {
            if (!level.isClientSide() && state.getDestroySpeed(level, pos) != 0.0F && !isTooDamagedToThrow(stack)) {
                stack.hurtAndBreak(1, miningEntity, EquipmentSlot.MAINHAND);
            }
            if (level instanceof ServerLevel serverLevel && miningEntity instanceof ServerPlayer serverPlayer) {
                VeinHarvestHelper.harvestConnectedVeinFromHand(serverLevel, serverPlayer, stack, pos, state);
            }
            return true;
        }
        return super.mineBlock(stack, level, state, pos, miningEntity);
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
        int ricochetLevel = ModEnchantments.getLevel(level, stack, ModEnchantments.RICOCHET);
        int tailwindLevel = ModEnchantments.getLevel(level, stack, ModEnchantments.TAILWIND);

        float baseDamage = flight.thrownDamage.get().floatValue();
        double speed = flight.throwSpeed.get() * (1.0 + tailwindLevel * ThrowingDiscConfig.COMMON.tailwindSpeedBonusPerLevel.get());
        double maxRange = flight.maxRange.get() + ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraRangePerLevel.get();
        int maxBounces = flight.maxBounces.get() + ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraBouncesPerLevel.get();
        int cooldown = flight.cooldownTicks.get();

        ItemStack resultStack = stack;
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ItemStack thrownStack = stack.copyWithCount(1);
            if (!player.hasInfiniteMaterials()) {
                ServerPlayer serverPlayer = player instanceof ServerPlayer sp ? sp : null;
                thrownStack.hurtAndBreak(1, serverLevel, serverPlayer, broken -> {});
            }

            int originSlot = hand == InteractionHand.OFF_HAND ? ThrownDiscEntity.SLOT_OFFHAND : player.getInventory().selected;
            ThrownDiscEntity discEntity = new ThrownDiscEntity(level, player, thrownStack, originSlot);
            discEntity.configureFlight(baseDamage, speed, maxRange, maxBounces, player.hasInfiniteMaterials());
            discEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float) speed, 0.15F);
            level.addFreshEntity(discEntity);

            level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.TRIDENT_THROW.value(),
                SoundSource.PLAYERS,
                0.85F,
                1.35F
            );

            if (!player.hasInfiniteMaterials()) {
                if (ThrowingDiscConfig.COMMON.useSlotPlaceholder.get()) {
                    resultStack = DiscPlaceholderItem.createFor(discEntity, stack, level.getGameTime());
                    player.setItemInHand(hand, resultStack);
                } else {
                    stack.shrink(1);
                    resultStack = stack;
                }
            }
        } else if (!player.hasInfiniteMaterials() && ThrowingDiscConfig.COMMON.useSlotPlaceholder.get()) {
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
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        if (enchantment.is(Enchantments.SWEEPING_EDGE)) {
            return false;
        }
        if (enchantment.is(Enchantments.FORTUNE) || enchantment.is(Enchantments.SILK_TOUCH)) {
            return stack.getTagEnchantments().keySet().stream().anyMatch(h -> h.is(ModEnchantments.CLEAVING_HARVEST));
        }
        return super.supportsEnchantment(stack, enchantment);
    }

    @Override
    public boolean isPrimaryItemFor(ItemStack stack, Holder<Enchantment> enchantment) {
        return !enchantment.is(Enchantments.SWEEPING_EDGE) && super.isPrimaryItemFor(stack, enchantment);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ThrowingDiscConfig.FlightStats flight = ThrowingDiscConfig.COMMON.flight(this.defaults);
        int ricochetLevel = ModEnchantments.getLevel(level, stack, ModEnchantments.RICOCHET);
        int tailwindLevel = ModEnchantments.getLevel(level, stack, ModEnchantments.TAILWIND);

        float baseDamage = flight.thrownDamage.get().floatValue();
        double speed = flight.throwSpeed.get() * (1.0 + tailwindLevel * ThrowingDiscConfig.COMMON.tailwindSpeedBonusPerLevel.get());
        double maxRange = flight.maxRange.get() + ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraRangePerLevel.get();
        int maxBounces = flight.maxBounces.get() + ricochetLevel * ThrowingDiscConfig.COMMON.ricochetExtraBouncesPerLevel.get();

        ItemStack copy = stack.copyWithCount(1);
        if (level instanceof ServerLevel serverLevel && !isTooDamagedToThrow(copy)) {
            copy.hurtAndBreak(1, serverLevel, (LivingEntity) null, broken -> {});
        }
        ThrownDiscEntity entity = new ThrownDiscEntity(level, pos.x(), pos.y(), pos.z(), copy);
        entity.configureFlight(baseDamage, speed, maxRange, maxBounces, false);
        return entity;
    }

    @Override
    public ProjectileItem.DispenseConfig createDispenseConfig() {
        ThrowingDiscConfig.FlightStats flight = ThrowingDiscConfig.COMMON.flight(this.defaults);
        float speed = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? flight.throwSpeed.get().floatValue()
            : (float) this.defaults.throwSpeed();
        return ProjectileItem.DispenseConfig.builder()
            .power(speed)
            .uncertainty(1.5F)
            .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        double damage = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? ThrowingDiscConfig.COMMON.flight(this.defaults).thrownDamage.get()
            : this.defaults.thrownDamage();
        double range = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? ThrowingDiscConfig.COMMON.flight(this.defaults).maxRange.get()
            : this.defaults.maxRange();
        int bounces = ThrowingDiscConfig.COMMON_SPEC.isLoaded()
            ? ThrowingDiscConfig.COMMON.flight(this.defaults).maxBounces.get()
            : this.defaults.maxBounces();

        HolderLookup.Provider registries = context.registries();
        if (registries != null && ThrowingDiscConfig.COMMON_SPEC.isLoaded()) {
            int ricochetLevel = ModEnchantments.getLevel(registries, stack, ModEnchantments.RICOCHET);
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
