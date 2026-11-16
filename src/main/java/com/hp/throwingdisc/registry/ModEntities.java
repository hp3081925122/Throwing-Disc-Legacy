package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.entity.ThrownDiscEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(
        ForgeRegistries.ENTITY_TYPES,
        Throwingdisc.MOD_ID
    );

    public static final RegistryObject<EntityType<ThrownDiscEntity>> THROWN_DISC = ENTITY_TYPES.register(
        "thrown_disc",
        () -> EntityType.Builder.<ThrownDiscEntity>of(ThrownDiscEntity::new, MobCategory.MISC)
            .sized(0.55F, 0.25F)
            .clientTrackingRange(8)
            .updateInterval(1)
            .build(Throwingdisc.id("thrown_disc").toString())
    );

    private ModEntities() {
    }
}
