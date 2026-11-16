package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> THROWING_DISC = ResourceKey.create(
        Registries.DAMAGE_TYPE,
        Throwingdisc.id("throwing_disc")
    );

    private ModDamageTypes() {
    }

    public static DamageSource throwingDisc(Level level, Entity directEntity, @Nullable Entity owner) {
        return level.registryAccess()
            .lookup(Registries.DAMAGE_TYPE)
            .flatMap(lookup -> lookup.get(THROWING_DISC))
            .map(holder -> new DamageSource(holder, directEntity, owner == null ? directEntity : owner))
            .orElseGet(() -> level.damageSources().trident(directEntity, owner == null ? directEntity : owner));
    }
}
