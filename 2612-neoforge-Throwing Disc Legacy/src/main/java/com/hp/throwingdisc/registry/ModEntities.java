package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.entity.ThrownDiscEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(Throwingdisc.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownDiscEntity>> THROWN_DISC = ENTITY_TYPES.registerEntityType(
        "thrown_disc",
        ThrownDiscEntity::new,
        MobCategory.MISC,
        builder -> builder
            .sized(0.55F, 0.25F)
            .clientTrackingRange(8)
            .updateInterval(1)
    );

    private ModEntities() {
    }
}
