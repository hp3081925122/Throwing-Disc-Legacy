package com.hp.throwingdisc.entity;

import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.item.DiscPlaceholderItem;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import com.hp.throwingdisc.registry.ModDamageTypes;
import com.hp.throwingdisc.registry.ModEnchantments;
import com.hp.throwingdisc.registry.ModEntities;
import com.hp.throwingdisc.registry.ModItems;
import com.hp.throwingdisc.util.VeinHarvestHelper;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DamageResistant;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.slf4j.Logger;

public class ThrownDiscEntity extends Projectile implements ItemSupplier {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int SLOT_NONE = -1;
    public static final int SLOT_OFFHAND = -2;

    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK = SynchedEntityData.defineId(
        ThrownDiscEntity.class, EntityDataSerializers.ITEM_STACK
    );
    private static final EntityDataAccessor<Boolean> DATA_RETURNING = SynchedEntityData.defineId(
        ThrownDiscEntity.class, EntityDataSerializers.BOOLEAN
    );
    private static final EntityDataAccessor<ItemStack> DATA_CARGO_PREVIEW = SynchedEntityData.defineId(
        ThrownDiscEntity.class, EntityDataSerializers.ITEM_STACK
    );
    private static final DustParticleOptions OUTBOUND_TRAIL_PARTICLE = new DustParticleOptions(0xDCEBFF, 0.45F);
    private static final DustParticleOptions RETURN_TRAIL_PARTICLE = new DustParticleOptions(0x6CE8FF, 0.45F);

    private final IntSet hitEntityIds = new IntOpenHashSet();
    private final List<ItemStack> carriedDrops = new ArrayList<>();
    private int carriedXp = 0;
    private int blocksBroken = 0;
    private int bounceCount = 0;
    private int maxBounces = 4;
    private double traveledDistance = 0.0;
    private double maxRange = 14.0;
    private float baseDamage = 6.0F;
    private double initialSpeed = 0.74;
    private double returnSpeed = 0.74;
    private int returnTicks = 0;
    private int liveTicks = 0;
    private int originSlot = SLOT_NONE;
    private boolean creativePickupOnly = false;
    private boolean fromDispenser = false;

    public ThrownDiscEntity(EntityType<? extends ThrownDiscEntity> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownDiscEntity(Level level, LivingEntity shooter, ItemStack stack, int originSlot) {
        super(ModEntities.THROWN_DISC.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.2, shooter.getZ());
        this.setItem(stack);
        this.originSlot = originSlot;
    }

    public ThrownDiscEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntities.THROWN_DISC.get(), level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.fromDispenser = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM_STACK, new ItemStack(ModItems.IRON_THROWING_DISC.get()));
        builder.define(DATA_RETURNING, false);
        builder.define(DATA_CARGO_PREVIEW, ItemStack.EMPTY);
    }

    public void configureFlight(float baseDamage, double initialSpeed, double maxRange, int maxBounces, boolean creativeOnly) {
        this.baseDamage = baseDamage;
        this.initialSpeed = initialSpeed;
        this.maxRange = maxRange;
        this.maxBounces = maxBounces;
        this.creativePickupOnly = creativeOnly;
    }

    public void setItem(ItemStack stack) {
        if (!stack.isEmpty()) {
            this.getEntityData().set(DATA_ITEM_STACK, stack.copyWithCount(1));
        }
    }

    @Override
    public ItemStack getItem() {
        return this.getEntityData().get(DATA_ITEM_STACK);
    }

    public ItemStack getCargoPreview() {
        return this.getEntityData().get(DATA_CARGO_PREVIEW);
    }

    private void refreshCargoPreview() {
        ItemStack preview = this.carriedDrops.isEmpty() ? ItemStack.EMPTY : this.carriedDrops.get(0).copyWithCount(1);
        this.getEntityData().set(DATA_CARGO_PREVIEW, preview);
    }

    public boolean isReturning() {
        return this.getEntityData().get(DATA_RETURNING);
    }

    public void setReturning(boolean returning) {
        if (this.isReturning() != returning) {
            this.getEntityData().set(DATA_RETURNING, returning);
            this.hitEntityIds.clear();
        }
    }

    @Override
    public boolean fireImmune() {
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            DamageResistant resistant = stack.get(DataComponents.DAMAGE_RESISTANT);
            if (resistant != null && resistant.types().equals(DamageTypeTags.IS_FIRE)) {
                return true;
            }
        }
        return super.fireImmune();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!target.canBeHitByProjectile()) {
            return false;
        }
        Entity owner = this.getOwner();
        if (owner != null && (target == owner || owner.isAlliedTo(target))) {
            return false;
        }
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        this.liveTicks++;

        if (this.level().isClientSide()) {
            Vec3 startPos = this.position();
            Vec3 vel = this.getDeltaMovement();
            Vec3 nextPos = startPos.add(vel);
            if (!this.isReturning() && vel.lengthSqr() > 1.0E-5) {
                BlockHitResult hit = this.level().clip(new ClipContext(
                    startPos,
                    nextPos,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    this
                ));
                if (hit.getType() != HitResult.Type.MISS) {
                    nextPos = hit.getLocation().subtract(vel.normalize().scale(0.05));
                }
            }
            this.setPos(nextPos.x, nextPos.y, nextPos.z);
            this.updateRotationFromVelocity(vel);
            if (ThrowingDiscConfig.CLIENT.showBounceParticles.get()) {
                DustParticleOptions trail = this.isReturning() ? RETURN_TRAIL_PARTICLE : OUTBOUND_TRAIL_PARTICLE;
                Vec3 midPos = startPos.lerp(nextPos, 0.5);
                this.level().addParticle(trail, midPos.x, midPos.y + 0.08, midPos.z, 0.0, 0.0, 0.0);
                this.level().addParticle(trail, nextPos.x, nextPos.y + 0.08, nextPos.z, 0.0, 0.0, 0.0);
            }
            return;
        }

        if (this.blocksBroken > 0 && !this.carriedDrops.isEmpty()) {
            this.collectFreshSupportDrops(this.position(), 1.65);
        }

        ItemStack stack = this.getItem();
        boolean hasMagnetic = ModEnchantments.getLevel(this.level(), stack, ModEnchantments.MAGNETIC_CATCH) > 0;
        if (hasMagnetic) {
            this.collectMagneticDropsAlongSegment(this.position(), this.position());
            Entity owner = this.getOwner();
            if (!this.isReturning() && this.liveTicks >= 4 && owner instanceof Player player && player.isShiftKeyDown()) {
                this.startReturning();
            }
        }

        if (this.isReturning()) {
            this.tickReturning(hasMagnetic);
        } else {
            this.tickOutbound(hasMagnetic);
        }
    }

    private void tickOutbound(boolean hasMagnetic) {
        Vec3 currentPos = this.position();
        Vec3 remainingMove = this.getDeltaMovement();
        if (this.isInWater()) {
            remainingMove = remainingMove.scale(0.92);
            this.setDeltaMovement(remainingMove);
        }

        if (remainingMove.lengthSqr() < 1.0E-4) {
            if (hasMagnetic) {
                this.collectMagneticDropsAlongSegment(currentPos, currentPos);
            }
            this.finishOutbound();
            return;
        }

        for (int step = 0; step < 5 && remainingMove.lengthSqr() > 1.0E-5 && !this.isReturning() && !this.isRemoved(); step++) {
            Vec3 targetPos = currentPos.add(remainingMove);
            BlockHitResult blockHit = this.level().clip(new ClipContext(
                currentPos,
                targetPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this
            ));

            if (blockHit.getType() != HitResult.Type.MISS) {
                if (EventHooks.onProjectileImpact(this, blockHit)) {
                    this.finishOutbound();
                    return;
                }

                Vec3 hitLocation = blockHit.getLocation();
                Vec3 moveDir = remainingMove.normalize();
                Vec3 safeStop = hitLocation.subtract(moveDir.scale(0.05));
                double segDist = currentPos.distanceTo(safeStop);
                if (hasMagnetic) {
                    this.collectMagneticDropsAlongSegment(currentPos, hitLocation);
                }
                if (this.hitEntitiesAlongSegment(currentPos, hitLocation)) {
                    this.updateRotationFromVelocity(this.getDeltaMovement());
                    return;
                }
                this.traveledDistance += segDist;

                BlockPos hitPos = blockHit.getBlockPos();
                BlockState hitState = this.level().getBlockState(hitPos);

                if (this.level() instanceof ServerLevel serverLevel && this.tryHarvestBlock(serverLevel, hitPos, hitState)) {
                    double stepForward = Math.min(remainingMove.length(), currentPos.distanceTo(hitLocation) + 0.08);
                    Vec3 nextStart = currentPos.add(moveDir.scale(stepForward));
                    if (hasMagnetic) {
                        this.collectMagneticDropsAlongSegment(currentPos, nextStart);
                    }
                    double leftover = Math.max(0.0, remainingMove.length() - stepForward);

                    double postSpeed = Math.max(0.28, this.getDeltaMovement().length() * 0.95);
                    this.setDeltaMovement(moveDir.scale(postSpeed));
                    this.needsSync = true;
                    currentPos = nextStart;
                    remainingMove = moveDir.scale(leftover * 0.95);

                    int cleavingLevel = ModEnchantments.getLevel(serverLevel, this.getItem(), ModEnchantments.CLEAVING_HARVEST);
                    int maxBreakCount = cleavingLevel * ThrowingDiscConfig.COMMON.cleavingBlocksPerLevel.get();
                    boolean outOfDurability = !this.creativePickupOnly && ThrowingDiscItem.isTooDamagedToThrow(this.getItem());
                    if (this.blocksBroken >= maxBreakCount || outOfDurability || this.traveledDistance >= this.maxRange) {
                        this.setPos(currentPos);
                        this.finishOutbound();
                        return;
                    }
                    continue;
                }

                hitState.onProjectileHit(this.level(), hitState, blockHit, this);

                if (this.bounceCount < this.maxBounces && this.traveledDistance < this.maxRange) {
                    this.bounceCount++;
                    this.hitEntityIds.clear();
                    Vec3 normal = blockHit.getDirection().getUnitVec3();
                    Vec3 reflectedDir = moveDir.subtract(normal.scale(2.0 * moveDir.dot(normal))).normalize();
                    double newSpeed = Math.max(0.28, this.getDeltaMovement().length() * 0.96);
                    Vec3 newVelocity = reflectedDir.scale(newSpeed);
                    this.setDeltaMovement(newVelocity);
                    this.needsSync = true;
                    this.spawnBounceEffects(hitLocation, hitState, reflectedDir);

                    currentPos = hitLocation.add(normal.scale(0.06));
                    break;
                } else {
                    this.spawnBounceEffects(hitLocation, hitState, moveDir.scale(-1.0));
                    currentPos = safeStop;
                    this.setPos(currentPos);
                    this.finishOutbound();
                    return;
                }
            } else {
                if (hasMagnetic) {
                    this.collectMagneticDropsAlongSegment(currentPos, targetPos);
                }
                if (this.hitEntitiesAlongSegment(currentPos, targetPos)) {
                    this.updateRotationFromVelocity(this.getDeltaMovement());
                    return;
                }
                this.traveledDistance += remainingMove.length();
                currentPos = targetPos;
                break;
            }
        }

        if (!this.isRemoved() && !this.isReturning()) {
            this.setPos(currentPos);
            this.updateRotationFromVelocity(this.getDeltaMovement());
            if (this.traveledDistance >= this.maxRange || this.liveTicks > 240) {
                this.finishOutbound();
            }
        }
    }

    private boolean tryHarvestBlock(ServerLevel serverLevel, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return true;
        }

        ItemStack weaponStack = this.getItem();
        int cleavingLevel = ModEnchantments.getLevel(serverLevel, weaponStack, ModEnchantments.CLEAVING_HARVEST);
        if (cleavingLevel <= 0) {
            return false;
        }

        int maxBreakCount = cleavingLevel * ThrowingDiscConfig.COMMON.cleavingBlocksPerLevel.get();
        if (this.blocksBroken >= maxBreakCount) {
            return false;
        }

        if (!this.creativePickupOnly && ThrowingDiscItem.isTooDamagedToThrow(weaponStack)) {
            return false;
        }

        float destroySpeed = state.getDestroySpeed(serverLevel, pos);
        double maxHardness = ThrowingDiscConfig.COMMON.cleavingBaseHardness.get()
            + cleavingLevel * ThrowingDiscConfig.COMMON.cleavingHardnessPerLevel.get();
        if (destroySpeed < 0.0F || destroySpeed > maxHardness) {
            return false;
        }

        if (state.getBlock() instanceof GameMasterBlock) {
            return false;
        }

        if (state.hasBlockEntity() && !ThrowingDiscConfig.COMMON.cleavingBreakBlockEntities.get()) {
            return false;
        }

        if (weaponStack.getItem() instanceof ThrowingDiscItem discItem
            && state.is(discItem.defaults().baseMaterial().incorrectBlocksForDrops())) {
            return false;
        }

        Entity owner = this.getOwner();
        if (owner instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.blockActionRestricted(serverLevel, pos, serverPlayer.gameMode.getGameModeForPlayer())
                || !serverLevel.mayInteract(serverPlayer, pos)) {
                return false;
            }
            BreakBlockEvent breakEvent = new BreakBlockEvent(serverLevel, pos, state, serverPlayer);
            NeoForge.EVENT_BUS.post(breakEvent);
            if (breakEvent.isCanceled()) {
                return false;
            }
        } else {
            if (!ThrowingDiscConfig.COMMON.cleavingAllowDispenser.get() || !EventHooks.canEntityGrief(serverLevel, this)) {
                return false;
            }
        }

        BlockEntity blockEntity = state.hasBlockEntity() ? serverLevel.getBlockEntity(pos) : null;
        List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, owner, weaponStack);
        int xp = state.getExpDrop(serverLevel, pos, blockEntity, owner, weaponStack);

        boolean removed;
        if (owner instanceof Player player) {
            BlockState adjusted = state.getBlock().playerWillDestroy(serverLevel, pos, state, player);
            serverLevel.levelEvent(2001, pos, Block.getId(state));
            removed = adjusted.onDestroyedByPlayer(serverLevel, pos, player, weaponStack, true, serverLevel.getFluidState(pos));
            if (removed) {
                adjusted.getBlock().destroy(serverLevel, pos, adjusted);
            }
        } else {
            serverLevel.levelEvent(2001, pos, Block.getId(state));
            removed = serverLevel.setBlock(pos, serverLevel.getFluidState(pos).createLegacyBlock(), 3);
        }

        if (!removed) {
            return false;
        }

        serverLevel.gameEvent(GameEvent.BLOCK_DESTROY, pos, GameEvent.Context.of(owner, state));

        for (ItemStack drop : drops) {
            this.addCarriedDrop(drop);
        }
        if (xp > 0) {
            this.carriedXp += xp;
        }
        this.collectFreshSupportDrops(Vec3.atCenterOf(pos), 1.65);

        if (!this.creativePickupOnly && !(owner instanceof Player p && p.hasInfiniteMaterials())) {
            ItemStack updated = weaponStack.copy();
            ServerPlayer sp = owner instanceof ServerPlayer serverPlayer ? serverPlayer : null;
            updated.hurtAndBreak(1, serverLevel, sp, broken -> {});
            this.setItem(updated);
        }

        this.blocksBroken++;

        int veinLevel = ModEnchantments.getLevel(serverLevel, this.getItem(), ModEnchantments.VEIN_HARVEST);
        if (veinLevel > 0 && VeinHarvestHelper.isOre(state)) {
            double veinMaxHardness = Math.max(destroySpeed + 2.0, maxHardness);
            int maxVeinExtra = veinLevel * ThrowingDiscConfig.COMMON.veinHarvestBlocksPerLevel.get();
            this.harvestConnectedVein(serverLevel, pos, state, veinMaxHardness, maxVeinExtra);
        }

        return true;
    }

    private void harvestConnectedVein(
        ServerLevel serverLevel,
        BlockPos originPos,
        BlockState originState,
        double maxHardness,
        int maxExtra
    ) {
        if (maxExtra <= 0) {
            return;
        }

        Entity owner = this.getOwner();
        boolean infiniteMaterials = this.creativePickupOnly || (owner instanceof Player p && p.hasInfiniteMaterials());

        LongSet visited = new LongOpenHashSet();
        Queue<BlockPos> queue = new ArrayDeque<>();
        visited.add(originPos.asLong());
        queue.add(originPos.immutable());

        int chainedCount = 0;
        while (!queue.isEmpty() && chainedCount < maxExtra) {
            if (!infiniteMaterials && ThrowingDiscItem.isTooDamagedToThrow(this.getItem())) {
                break;
            }

            BlockPos current = queue.poll();
            for (int dx = -1; dx <= 1 && chainedCount < maxExtra; dx++) {
                for (int dy = -1; dy <= 1 && chainedCount < maxExtra; dy++) {
                    for (int dz = -1; dz <= 1 && chainedCount < maxExtra; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) {
                            continue;
                        }
                        if (!infiniteMaterials && ThrowingDiscItem.isTooDamagedToThrow(this.getItem())) {
                            break;
                        }

                        BlockPos nextPos = current.offset(dx, dy, dz);
                        if (Math.abs(nextPos.getX() - originPos.getX()) > 16
                            || Math.abs(nextPos.getY() - originPos.getY()) > 16
                            || Math.abs(nextPos.getZ() - originPos.getZ()) > 16) {
                            continue;
                        }
                        if (!visited.add(nextPos.asLong())) {
                            continue;
                        }

                        BlockState nextState = serverLevel.getBlockState(nextPos);
                        if (!VeinHarvestHelper.isSameOreType(originState, nextState)) {
                            continue;
                        }

                        float destroySpeed = nextState.getDestroySpeed(serverLevel, nextPos);
                        if (destroySpeed < 0.0F || destroySpeed > maxHardness) {
                            continue;
                        }
                        if (nextState.getBlock() instanceof GameMasterBlock) {
                            continue;
                        }
                        if (nextState.hasBlockEntity() && !ThrowingDiscConfig.COMMON.cleavingBreakBlockEntities.get()) {
                            continue;
                        }

                        ItemStack currentWeapon = this.getItem();
                        if (currentWeapon.getItem() instanceof ThrowingDiscItem discItem
                            && nextState.is(discItem.defaults().baseMaterial().incorrectBlocksForDrops())) {
                            continue;
                        }

                        if (owner instanceof ServerPlayer serverPlayer) {
                            if (serverPlayer.blockActionRestricted(serverLevel, nextPos, serverPlayer.gameMode.getGameModeForPlayer())
                                || !serverLevel.mayInteract(serverPlayer, nextPos)) {
                                continue;
                            }
                            BreakBlockEvent breakEvent = new BreakBlockEvent(serverLevel, nextPos, nextState, serverPlayer);
                            NeoForge.EVENT_BUS.post(breakEvent);
                            if (breakEvent.isCanceled()) {
                                continue;
                            }
                        } else {
                            if (!ThrowingDiscConfig.COMMON.cleavingAllowDispenser.get() || !EventHooks.canEntityGrief(serverLevel, this)) {
                                continue;
                            }
                        }

                        BlockEntity blockEntity = nextState.hasBlockEntity() ? serverLevel.getBlockEntity(nextPos) : null;
                        List<ItemStack> drops = Block.getDrops(nextState, serverLevel, nextPos, blockEntity, owner, currentWeapon);
                        int xp = nextState.getExpDrop(serverLevel, nextPos, blockEntity, owner, currentWeapon);

                        boolean removed;
                        if (owner instanceof Player player) {
                            BlockState adjusted = nextState.getBlock().playerWillDestroy(serverLevel, nextPos, nextState, player);
                            if (chainedCount < 6) {
                                serverLevel.levelEvent(2001, nextPos, Block.getId(nextState));
                            }
                            removed = adjusted.onDestroyedByPlayer(serverLevel, nextPos, player, currentWeapon, true, serverLevel.getFluidState(nextPos));
                            if (removed) {
                                adjusted.getBlock().destroy(serverLevel, nextPos, adjusted);
                            }
                        } else {
                            if (chainedCount < 6) {
                                serverLevel.levelEvent(2001, nextPos, Block.getId(nextState));
                            }
                            removed = serverLevel.setBlock(nextPos, serverLevel.getFluidState(nextPos).createLegacyBlock(), 3);
                        }

                        if (!removed) {
                            continue;
                        }

                        serverLevel.gameEvent(GameEvent.BLOCK_DESTROY, nextPos, GameEvent.Context.of(owner, nextState));

                        for (ItemStack drop : drops) {
                            this.addCarriedDrop(drop);
                        }
                        if (xp > 0) {
                            this.carriedXp += xp;
                        }
                        this.collectFreshSupportDrops(Vec3.atCenterOf(nextPos), 1.65);

                        if (!infiniteMaterials) {
                            ItemStack updated = currentWeapon.copy();
                            ServerPlayer sp = owner instanceof ServerPlayer serverPlayer ? serverPlayer : null;
                            updated.hurtAndBreak(1, serverLevel, sp, broken -> {});
                            this.setItem(updated);
                        }

                        chainedCount++;
                        queue.add(nextPos.immutable());
                    }
                }
            }
        }

        if (chainedCount > 0) {
            LOGGER.debug("Vein Harvest (thrown) chained {} ore block(s) from {}", chainedCount, originPos);
        }
    }

    private void addCarriedDrop(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack remaining = stack.copy();
        for (ItemStack existing : this.carriedDrops) {
            if (ItemStack.isSameItemSameComponents(existing, remaining) && existing.getCount() < existing.getMaxStackSize()) {
                int space = existing.getMaxStackSize() - existing.getCount();
                int move = Math.min(space, remaining.getCount());
                existing.grow(move);
                remaining.shrink(move);
                if (remaining.isEmpty()) {
                    this.refreshCargoPreview();
                    return;
                }
            }
        }
        if (!remaining.isEmpty()) {
            this.carriedDrops.add(remaining);
            this.refreshCargoPreview();
        }
    }

    private void collectFreshSupportDrops(Vec3 center, double radius) {
        AABB box = new AABB(
            center.x - radius, center.y - radius, center.z - radius,
            center.x + radius, center.y + radius + 1.5, center.z + radius
        );
        for (ItemEntity itemEntity : this.level().getEntitiesOfClass(ItemEntity.class, box, e -> e.isAlive() && e.getAge() <= 3)) {
            ItemStack item = itemEntity.getItem();
            if (!item.isEmpty()) {
                this.addCarriedDrop(item);
                itemEntity.discard();
            }
        }
    }

    private void finishOutbound() {
        if (this.fromDispenser || this.getOwner() == null) {
            this.dropAsItemAndDiscard();
        } else {
            this.startReturning();
        }
    }

    public void startReturning() {
        Entity owner = this.getOwner();
        if (this.fromDispenser || owner == null) {
            this.dropAsItemAndDiscard();
            return;
        }
        double currentSpeed = this.getDeltaMovement().length();
        double baseReturnSpeed = Math.min(this.initialSpeed, Math.max(this.initialSpeed * 0.6, currentSpeed));
        int tailwindLevel = ModEnchantments.getLevel(this.level(), this.getItem(), ModEnchantments.TAILWIND);
        double returnMult = ThrowingDiscConfig.COMMON.returnSpeedMultiplier.get()
            + tailwindLevel * ThrowingDiscConfig.COMMON.tailwindReturnBonusPerLevel.get();
        this.returnSpeed = Math.max(0.28, baseReturnSpeed * returnMult);

        this.setReturning(true);
        this.noPhysics = true;
        this.returnTicks = 0;

        Vec3 anchor = new Vec3(owner.getX(), owner.getY() + owner.getBbHeight() * 0.6, owner.getZ());
        Vec3 toOwner = anchor.subtract(this.position());
        if (toOwner.lengthSqr() > 1.0E-5) {
            Vec3 returnVel = toOwner.normalize().scale(this.returnSpeed);
            this.setDeltaMovement(returnVel);
            this.updateRotationFromVelocity(returnVel);
        }
        this.needsSync = true;
        this.level().playSound(
            null,
            this.getX(),
            this.getY(),
            this.getZ(),
            SoundEvents.TRIDENT_RETURN,
            SoundSource.PLAYERS,
            0.75F,
            1.35F
        );
    }

    private void tickReturning(boolean hasMagnetic) {
        this.noPhysics = true;
        this.returnTicks++;
        Entity owner = this.getOwner();

        if (owner == null || !owner.isAlive() || owner.level() != this.level() || (owner instanceof Player p && p.isSpectator())) {
            this.dropAsItemAndDiscard();
            return;
        }

        if (this.returnTicks > ThrowingDiscConfig.COMMON.maxReturnTicks.get()) {
            if (owner instanceof Player player) {
                this.giveBackToPlayer(player);
            } else {
                this.dropAsItemAndDiscard();
            }
            return;
        }

        Vec3 anchor = new Vec3(owner.getX(), owner.getY() + owner.getBbHeight() * 0.6, owner.getZ());
        Vec3 currentPos = this.position();
        Vec3 toOwner = anchor.subtract(currentPos);
        double dist = toOwner.length();
        double speed = this.returnSpeed > 0.0 ? this.returnSpeed : Math.max(0.28, this.initialSpeed);

        if (dist <= speed + 0.45) {
            this.hitEntitiesAlongSegment(currentPos, anchor);
            if (hasMagnetic) {
                this.collectMagneticDropsAlongSegment(currentPos, anchor);
            }
            if (owner instanceof Player player) {
                this.giveBackToPlayer(player);
            } else {
                this.spillCarriedLoot(currentPos, null);
                this.discard();
            }
            return;
        }

        Vec3 nextVel = toOwner.normalize().scale(speed);
        Vec3 nextPos = currentPos.add(nextVel);
        this.hitEntitiesAlongSegment(currentPos, nextPos);
        if (hasMagnetic) {
            this.collectMagneticDropsAlongSegment(currentPos, nextPos);
        }
        this.setDeltaMovement(nextVel);
        this.needsSync = true;
        this.setPos(nextPos);
        this.updateRotationFromVelocity(nextVel);
    }

    private boolean hitEntitiesAlongSegment(Vec3 start, Vec3 end) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }

        AABB sweepBox = new AABB(start, end).inflate(0.6);
        List<Entity> candidates = serverLevel.getEntities(this, sweepBox, this::canHitEntity);
        if (candidates.isEmpty()) {
            return false;
        }

        candidates.sort(Comparator.comparingDouble(e -> e.distanceToSqr(start)));
        ItemStack weaponStack = this.getItem();
        Entity owner = this.getOwner();

        for (Entity rawTarget : candidates) {
            AABB targetBox = rawTarget.getBoundingBox().inflate(0.35);
            Vec3 contactPos = targetBox.contains(start) ? start : targetBox.clip(start, end).orElse(null);
            if (contactPos == null) {
                continue;
            }

            if (rawTarget.is(EntityTypeTags.REDIRECTABLE_PROJECTILE) && rawTarget instanceof Projectile redirectable) {
                redirectable.deflect(ProjectileDeflection.AIM_DEFLECT, owner, EntityReference.of(owner), true);
                continue;
            }

            Entity rootIdEntity = rawTarget instanceof EnderDragonPart part ? part.parentMob : rawTarget;
            if (this.hitEntityIds.contains(rootIdEntity.getId())) {
                continue;
            }
            this.hitEntityIds.add(rootIdEntity.getId());

            if (!this.isReturning() && rawTarget.deflection(this) != ProjectileDeflection.NONE) {
                this.deflectOffEntity(serverLevel, rawTarget, rootIdEntity, contactPos);
                return true;
            }

            float damage = this.calculateHitDamage(serverLevel, weaponStack, rawTarget);
            DamageSource damageSource = ModDamageTypes.throwingDisc(serverLevel, this, owner);

            if ((this.bounceCount > 0 || this.isReturning()) && rawTarget.invulnerableTime > 0) {
                rawTarget.invulnerableTime = 0;
            }

            boolean wasBlocked = rawTarget instanceof LivingEntity blockingLiving && blockingLiving.getItemBlockingWith() != null;

            if (rawTarget.hurtServer(serverLevel, damageSource, damage)) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, rawTarget, damageSource, weaponStack);

                int fireAspect = serverLevel.registryAccess()
                    .lookup(Registries.ENCHANTMENT)
                    .flatMap(lookup -> lookup.get(Enchantments.FIRE_ASPECT))
                    .map(weaponStack::getEnchantmentLevel)
                    .orElse(0);
                if (fireAspect > 0) {
                    rawTarget.igniteForSeconds(4 * fireAspect);
                }

                if (rawTarget instanceof LivingEntity living) {
                    double kbResist = living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                    double strength = Math.max(0.0, 0.28 * (1.0 - kbResist));
                    Vec3 dir = this.getDeltaMovement();
                    if (strength > 0.0 && dir.horizontalDistanceSqr() > 1.0E-5) {
                        living.knockback(strength, -dir.x, -dir.z);
                    }
                }

                serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    rawTarget.getX(),
                    rawTarget.getY(0.5),
                    rawTarget.getZ(),
                    6,
                    0.15,
                    0.15,
                    0.15,
                    0.12
                );
                serverLevel.playSound(
                    null,
                    rawTarget.getX(),
                    rawTarget.getY(),
                    rawTarget.getZ(),
                    SoundEvents.TRIDENT_HIT,
                    SoundSource.PLAYERS,
                    0.85F,
                    1.25F
                );
            } else if (wasBlocked && !this.isReturning()) {
                this.deflectOffEntity(serverLevel, rawTarget, rootIdEntity, contactPos);
                return true;
            }

            if (!ThrowingDiscConfig.COMMON.pierceMultipleTargets.get() && !this.isReturning()) {
                this.setPos(contactPos);
                this.finishOutbound();
                return true;
            }
        }
        return false;
    }

    private void deflectOffEntity(ServerLevel serverLevel, Entity rawTarget, Entity rootIdEntity, Vec3 contactPos) {
        Vec3 move = this.getDeltaMovement();
        double speed = Math.max(0.28, move.length() * 0.92);
        Vec3 moveDir = move.lengthSqr() > 1.0E-6 ? move.normalize() : new Vec3(0.0, 0.0, 1.0);
        Vec3 center = rawTarget.getBoundingBox().getCenter();
        Vec3 away = contactPos.subtract(center);
        Vec3 normal = away.lengthSqr() > 1.0E-4 ? away.normalize() : moveDir.scale(-1.0);
        if (moveDir.dot(normal) >= -0.05) {
            normal = moveDir.scale(-1.0);
        }
        Vec3 reflectedDir = moveDir.subtract(normal.scale(2.0 * moveDir.dot(normal))).normalize();

        this.traveledDistance += this.position().distanceTo(contactPos);
        Vec3 safeContact = contactPos.add(normal.scale(0.08));
        this.setPos(safeContact);

        serverLevel.playSound(
            null,
            safeContact.x,
            safeContact.y,
            safeContact.z,
            SoundEvents.ANVIL_PLACE,
            SoundSource.PLAYERS,
            0.35F,
            1.85F
        );
        serverLevel.sendParticles(
            ParticleTypes.ELECTRIC_SPARK,
            safeContact.x,
            safeContact.y,
            safeContact.z,
            8,
            0.12,
            0.12,
            0.12,
            0.22
        );
        serverLevel.sendParticles(
            ParticleTypes.CRIT,
            safeContact.x,
            safeContact.y,
            safeContact.z,
            5,
            0.10,
            0.10,
            0.10,
            0.15
        );

        if (this.bounceCount < this.maxBounces && this.traveledDistance < this.maxRange) {
            this.bounceCount++;
            this.hitEntityIds.clear();
            this.hitEntityIds.add(rootIdEntity.getId());
            this.setDeltaMovement(reflectedDir.scale(speed));
            this.needsSync = true;
        } else {
            this.finishOutbound();
        }
    }

    private float calculateHitDamage(ServerLevel serverLevel, ItemStack weaponStack, Entity target) {
        float damage = this.baseDamage;
        if (this.isReturning()) {
            damage *= ThrowingDiscConfig.COMMON.returnDamageMultiplier.get().floatValue();
            int reboundLevel = ModEnchantments.getLevel(serverLevel, weaponStack, ModEnchantments.REBOUND_EDGE);
            if (reboundLevel > 0) {
                damage += (float) (reboundLevel * ThrowingDiscConfig.COMMON.reboundEdgeBonusDamagePerLevel.get());
            }
        }

        if (this.bounceCount > 0) {
            int ricochetLevel = ModEnchantments.getLevel(serverLevel, weaponStack, ModEnchantments.RICOCHET);
            if (ricochetLevel > 0) {
                double bonusPerBounce = ricochetLevel * ThrowingDiscConfig.COMMON.ricochetBounceDamageBonusPerLevel.get();
                double mult = Math.min(
                    ThrowingDiscConfig.COMMON.maxRicochetDamageMultiplier.get(),
                    1.0 + this.bounceCount * bonusPerBounce
                );
                damage *= (float) mult;
            }
        }

        DamageSource damageSource = ModDamageTypes.throwingDisc(serverLevel, this, this.getOwner());
        return EnchantmentHelper.modifyDamage(serverLevel, weaponStack, target, damageSource, damage);
    }

    private void collectMagneticDropsAlongSegment(Vec3 start, Vec3 end) {
        if (this.fromDispenser || !(this.level() instanceof ServerLevel)) {
            return;
        }
        Entity owner = this.getOwner();
        if (owner == null) {
            return;
        }

        double radius = ThrowingDiscConfig.COMMON.magneticRecallRadius.get();
        double radiusSqr = radius * radius;
        AABB searchBox = new AABB(start, end).inflate(radius);

        int capturedStacks = 0;
        int capturedXp = 0;

        for (ItemEntity itemEntity : this.level().getEntitiesOfClass(ItemEntity.class, searchBox, Entity::isAlive)) {
            if (distToSegmentSqr(itemEntity.getBoundingBox().getCenter(), start, end) > radiusSqr) {
                continue;
            }
            if (!this.canMagneticallyCapture(itemEntity, owner)) {
                continue;
            }
            ItemStack item = itemEntity.getItem();
            if (!item.isEmpty()) {
                this.addCarriedDrop(item);
                itemEntity.discard();
                capturedStacks++;
            }
        }

        for (ExperienceOrb orb : this.level().getEntitiesOfClass(ExperienceOrb.class, searchBox, Entity::isAlive)) {
            if (distToSegmentSqr(orb.getBoundingBox().getCenter(), start, end) > radiusSqr) {
                continue;
            }
            TagValueOutput orbOut = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            orb.saveWithoutId(orbOut);
            int orbCount = Math.max(1, orbOut.buildResult().getIntOr("Count", 1));
            int xpValue = Math.max(0, orb.getValue()) * orbCount;
            if (xpValue > 0) {
                this.carriedXp += xpValue;
                capturedXp += xpValue;
            }
            orb.discard();
        }

        if (capturedStacks > 0 || capturedXp > 0) {
            this.level().playSound(
                null,
                this.getX(),
                this.getY(),
                this.getZ(),
                SoundEvents.ITEM_PICKUP,
                SoundSource.PLAYERS,
                0.35F,
                1.45F + this.random.nextFloat() * 0.25F
            );
            LOGGER.debug(
                "Magnetic Recall captured {} item entity(ies) and {} XP along {} -> {} (returning={})",
                capturedStacks,
                capturedXp,
                start,
                end,
                this.isReturning()
            );
        }
    }

    private boolean canMagneticallyCapture(ItemEntity itemEntity, Entity owner) {
        ItemStack stack = itemEntity.getItem();
        if (stack.isEmpty()) {
            return false;
        }

        UUID target = itemEntity.getTarget();
        if (target != null && !target.equals(owner.getUUID())) {
            return false;
        }

        if (itemEntity.hasPickUpDelay()) {
            if (owner.equals(itemEntity.getOwner())) {
                return false;
            }
            TagValueOutput itemOut = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            itemEntity.saveWithoutId(itemOut);
            short pickupDelay = itemOut.buildResult().getShortOr("PickupDelay", (short) 0);
            if (pickupDelay < 0 || pickupDelay > 15) {
                return false;
            }
        }

        return true;
    }

    private static double distToSegmentSqr(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 ab = end.subtract(start);
        double lenSqr = ab.lengthSqr();
        if (lenSqr < 1.0E-7) {
            return point.distanceToSqr(start);
        }
        double t = Mth.clamp(point.subtract(start).dot(ab) / lenSqr, 0.0, 1.0);
        Vec3 closest = start.add(ab.scale(t));
        return point.distanceToSqr(closest);
    }

    private void spawnBounceEffects(Vec3 pos, BlockState hitState, Vec3 reflectedDir) {
        float pitch = Math.min(1.85F, 1.25F + this.bounceCount * 0.08F);
        this.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 0.75F, pitch);
        this.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.COPPER_HIT, SoundSource.PLAYERS, 0.65F, pitch + 0.15F);
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y, pos.z, 7, 0.06, 0.06, 0.06, 0.18);
            serverLevel.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 5, 0.08, 0.08, 0.08, 0.14);
            Vec3 guidePos = pos.add(reflectedDir.scale(0.28));
            serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, guidePos.x, guidePos.y, guidePos.z, 3, 0.03, 0.03, 0.03, 0.05);
            if (!hitState.isAir()) {
                serverLevel.sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, hitState),
                    pos.x,
                    pos.y,
                    pos.z,
                    6,
                    0.08,
                    0.08,
                    0.08,
                    0.08
                );
            }
        }
    }

    private void deliverCarriedLoot(Player player) {
        int deliveredStacks = this.carriedDrops.size();
        int deliveredXp = this.carriedXp;
        Inventory inv = player.getInventory();
        for (ItemStack loot : this.carriedDrops) {
            if (loot.isEmpty()) {
                continue;
            }
            ItemStack copy = loot.copy();
            inv.add(copy);
            if (!copy.isEmpty()) {
                ItemEntity drop = new ItemEntity(this.level(), player.getX(), player.getY() + 0.5, player.getZ(), copy);
                drop.setNoPickUpDelay();
                drop.setTarget(player.getUUID());
                this.level().addFreshEntity(drop);
            }
        }
        this.carriedDrops.clear();
        this.refreshCargoPreview();

        if (this.carriedXp > 0 && this.level() instanceof ServerLevel serverLevel) {
            ExperienceOrb.award(serverLevel, player.position(), this.carriedXp);
            this.carriedXp = 0;
        }
        if (deliveredStacks > 0 || deliveredXp > 0) {
            LOGGER.debug(
                "Delivered {} carried item stack(s) and {} XP to player {}",
                deliveredStacks,
                deliveredXp,
                player.getScoreboardName()
            );
        }
    }

    private void spillCarriedLoot(Vec3 pos, Player targetOwner) {
        if (!this.level().isClientSide()) {
            int spilledStacks = this.carriedDrops.size();
            int spilledXp = this.carriedXp;
            for (ItemStack loot : this.carriedDrops) {
                if (!loot.isEmpty()) {
                    ItemEntity drop = new ItemEntity(this.level(), pos.x, pos.y + 0.15, pos.z, loot.copy());
                    drop.setPickUpDelay(5);
                    if (targetOwner != null) {
                        drop.setTarget(targetOwner.getUUID());
                    }
                    this.level().addFreshEntity(drop);
                }
            }
            this.carriedDrops.clear();
            this.refreshCargoPreview();

            if (this.carriedXp > 0 && this.level() instanceof ServerLevel serverLevel) {
                ExperienceOrb.award(serverLevel, pos, this.carriedXp);
                this.carriedXp = 0;
            }
            if (spilledStacks > 0 || spilledXp > 0) {
                LOGGER.debug("Spilled {} carried item stack(s) and {} XP at {}", spilledStacks, spilledXp, pos);
            }
        }
    }

    private void giveBackToPlayer(Player player) {
        UUID discUuid = this.getUUID();
        if (this.creativePickupOnly || player.hasInfiniteMaterials()) {
            DiscPlaceholderItem.removeMatchingPlaceholders(player, discUuid);
            this.deliverCarriedLoot(player);
            this.playCatchSound(player);
            this.discard();
            return;
        }

        ItemStack stack = this.getItem().copy();
        if (stack.isEmpty()) {
            DiscPlaceholderItem.removeMatchingPlaceholders(player, discUuid);
            this.deliverCarriedLoot(player);
            this.discard();
            return;
        }

        boolean placed = false;
        Inventory inv = player.getInventory();
        if (this.originSlot == SLOT_OFFHAND && DiscPlaceholderItem.matches(player.getItemInHand(InteractionHand.OFF_HAND), discUuid)) {
            player.setItemInHand(InteractionHand.OFF_HAND, stack);
            placed = true;
        } else if (this.originSlot >= 0 && this.originSlot < inv.getContainerSize() && DiscPlaceholderItem.matches(inv.getItem(this.originSlot), discUuid)) {
            inv.setItem(this.originSlot, stack);
            placed = true;
        } else {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (DiscPlaceholderItem.matches(inv.getItem(i), discUuid)) {
                    inv.setItem(i, stack);
                    placed = true;
                    break;
                }
            }
        }

        DiscPlaceholderItem.removeMatchingPlaceholders(player, discUuid);

        if (!placed) {
            if (this.originSlot == SLOT_OFFHAND) {
                if (player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
                    player.setItemInHand(InteractionHand.OFF_HAND, stack);
                    placed = true;
                }
            } else if (this.originSlot >= 0 && this.originSlot < inv.getContainerSize()) {
                if (inv.getItem(this.originSlot).isEmpty()) {
                    inv.setItem(this.originSlot, stack);
                    placed = true;
                }
            }
        }

        if (!placed) {
            placed = inv.add(stack);
        }

        if (!placed && !stack.isEmpty()) {
            ItemEntity drop = new ItemEntity(this.level(), player.getX(), player.getY() + 0.5, player.getZ(), stack);
            drop.setNoPickUpDelay();
            drop.setTarget(player.getUUID());
            this.level().addFreshEntity(drop);
        }

        this.deliverCarriedLoot(player);
        player.inventoryMenu.broadcastChanges();
        this.playCatchSound(player);
        this.discard();
    }

    private void playCatchSound(Player player) {
        this.level().playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.ITEM_PICKUP,
            SoundSource.PLAYERS,
            0.65F,
            1.15F
        );
    }

    private void dropAsItemAndDiscard() {
        Player ownerPlayer = this.getOwner() instanceof Player p ? p : null;
        if (ownerPlayer != null && !this.level().isClientSide()) {
            DiscPlaceholderItem.removeMatchingPlaceholders(ownerPlayer, this.getUUID());
        }
        this.spillCarriedLoot(this.position(), ownerPlayer);
        if (!this.creativePickupOnly && !this.level().isClientSide()) {
            ItemStack stack = this.getItem().copy();
            if (!stack.isEmpty()) {
                ItemEntity drop = new ItemEntity(this.level(), this.getX(), this.getY() + 0.1, this.getZ(), stack);
                drop.setPickUpDelay(5);
                if (ownerPlayer != null) {
                    drop.setTarget(ownerPlayer.getUUID());
                }
                this.level().addFreshEntity(drop);
            }
        }
        this.discard();
    }

    private void updateRotationFromVelocity(Vec3 vel) {
        double horiz = vel.horizontalDistance();
        if (horiz > 1.0E-4 || Math.abs(vel.y) > 1.0E-4) {
            this.setYRot((float) (Mth.atan2(vel.x, vel.z) * (180.0 / Math.PI)));
            this.setXRot((float) (Mth.atan2(vel.y, horiz) * (180.0 / Math.PI)));
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double d0 = this.getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(d0)) {
            d0 = 10.0;
        }
        d0 *= 64.0;
        return distance < d0 * d0;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ItemStack stack = this.getItem();
        if (!stack.isEmpty()) {
            output.store("Item", ItemStack.CODEC, stack);
        }
        List<ItemStack> nonEmptyDrops = new ArrayList<>();
        for (ItemStack drop : this.carriedDrops) {
            if (!drop.isEmpty()) {
                nonEmptyDrops.add(drop);
            }
        }
        if (!nonEmptyDrops.isEmpty()) {
            output.store("CarriedDrops", ItemStack.CODEC.listOf(), nonEmptyDrops);
        }
        output.putInt("CarriedXp", this.carriedXp);
        output.putInt("BlocksBroken", this.blocksBroken);
        output.putBoolean("Returning", this.isReturning());
        output.putInt("BounceCount", this.bounceCount);
        output.putInt("MaxBounces", this.maxBounces);
        output.putDouble("TraveledDistance", this.traveledDistance);
        output.putDouble("MaxRange", this.maxRange);
        output.putFloat("BaseDamage", this.baseDamage);
        output.putDouble("InitialSpeed", this.initialSpeed);
        output.putDouble("ReturnSpeed", this.returnSpeed);
        output.putInt("ReturnTicks", this.returnTicks);
        output.putInt("OriginSlot", this.originSlot);
        output.putBoolean("CreativeOnly", this.creativePickupOnly);
        output.putBoolean("FromDispenser", this.fromDispenser);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("Item", ItemStack.CODEC).ifPresent(this::setItem);
        this.carriedDrops.clear();
        input.read("CarriedDrops", ItemStack.CODEC.listOf()).ifPresent(this.carriedDrops::addAll);
        this.refreshCargoPreview();
        this.carriedXp = input.getIntOr("CarriedXp", 0);
        this.blocksBroken = input.getIntOr("BlocksBroken", 0);
        this.setReturning(input.getBooleanOr("Returning", false));
        this.bounceCount = input.getIntOr("BounceCount", 0);
        this.maxBounces = input.getIntOr("MaxBounces", 4);
        this.traveledDistance = input.getDoubleOr("TraveledDistance", 0.0);
        this.maxRange = input.getDoubleOr("MaxRange", 14.0);
        this.baseDamage = input.getFloatOr("BaseDamage", 6.0F);
        this.initialSpeed = input.getDoubleOr("InitialSpeed", 0.74);
        this.returnSpeed = input.getDoubleOr("ReturnSpeed", this.initialSpeed);
        this.returnTicks = input.getIntOr("ReturnTicks", 0);
        this.originSlot = input.getIntOr("OriginSlot", SLOT_NONE);
        this.creativePickupOnly = input.getBooleanOr("CreativeOnly", false);
        this.fromDispenser = input.getBooleanOr("FromDispenser", false);
    }
}
