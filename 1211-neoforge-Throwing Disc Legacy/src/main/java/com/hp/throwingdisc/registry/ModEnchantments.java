package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.LevelReader;

public final class ModEnchantments {
    public static final ResourceKey<Enchantment> RICOCHET = create("ricochet");
    public static final ResourceKey<Enchantment> TAILWIND = create("tailwind");
    public static final ResourceKey<Enchantment> REBOUND_EDGE = create("rebound_edge");
    public static final ResourceKey<Enchantment> MAGNETIC_CATCH = create("magnetic_catch");
    public static final ResourceKey<Enchantment> CLEAVING_HARVEST = create("cleaving_harvest");
    public static final ResourceKey<Enchantment> VEIN_HARVEST = create("vein_harvest");

    private ModEnchantments() {
    }

    private static ResourceKey<Enchantment> create(String name) {
        return ResourceKey.create(Registries.ENCHANTMENT, Throwingdisc.id(name));
    }

    public static List<ResourceKey<Enchantment>> all() {
        return List.of(RICOCHET, TAILWIND, REBOUND_EDGE, MAGNETIC_CATCH, CLEAVING_HARVEST, VEIN_HARVEST);
    }

    public static int getLevel(LevelReader level, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return level.registryAccess()
            .lookup(Registries.ENCHANTMENT)
            .flatMap(lookup -> lookup.get(key))
            .map(stack::getEnchantmentLevel)
            .orElse(0);
    }

    public static int getLevel(HolderLookup.Provider registries, ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return registries.lookup(Registries.ENCHANTMENT)
            .flatMap(lookup -> lookup.get(key))
            .map(stack::getEnchantmentLevel)
            .orElse(0);
    }
}
