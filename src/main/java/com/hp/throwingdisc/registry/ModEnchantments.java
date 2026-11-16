package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import java.util.List;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(
        ForgeRegistries.ENCHANTMENTS,
        Throwingdisc.MOD_ID
    );

    public static final EnchantmentCategory THROWING_DISC_CATEGORY = EnchantmentCategory.create(
        "throwing_disc",
        item -> item instanceof ThrowingDiscItem
    );

    public static final RegistryObject<Enchantment> RICOCHET = ENCHANTMENTS.register(
        "ricochet",
        () -> new DiscEnchantment(Enchantment.Rarity.UNCOMMON, 3, 5, 8, 25)
    );
    public static final RegistryObject<Enchantment> TAILWIND = ENCHANTMENTS.register(
        "tailwind",
        () -> new DiscEnchantment(Enchantment.Rarity.UNCOMMON, 3, 5, 8, 25)
    );
    public static final RegistryObject<Enchantment> REBOUND_EDGE = ENCHANTMENTS.register(
        "rebound_edge",
        () -> new DiscEnchantment(Enchantment.Rarity.RARE, 3, 8, 9, 30)
    );
    public static final RegistryObject<Enchantment> MAGNETIC_CATCH = ENCHANTMENTS.register(
        "magnetic_catch",
        () -> new DiscEnchantment(Enchantment.Rarity.RARE, 1, 15, 0, 45)
    );
    public static final RegistryObject<Enchantment> CLEAVING_HARVEST = ENCHANTMENTS.register(
        "cleaving_harvest",
        () -> new DiscEnchantment(Enchantment.Rarity.RARE, 3, 12, 9, 36)
    );
    public static final RegistryObject<Enchantment> VEIN_HARVEST = ENCHANTMENTS.register(
        "vein_harvest",
        () -> new DiscEnchantment(Enchantment.Rarity.VERY_RARE, 3, 15, 10, 45)
    );

    private ModEnchantments() {
    }

    public static List<RegistryObject<Enchantment>> all() {
        return List.of(RICOCHET, TAILWIND, REBOUND_EDGE, MAGNETIC_CATCH, CLEAVING_HARVEST, VEIN_HARVEST);
    }

    public static int getLevel(ItemStack stack, RegistryObject<Enchantment> key) {
        if (stack.isEmpty()) {
            return 0;
        }
        return EnchantmentHelper.getItemEnchantmentLevel(key.get(), stack);
    }

    private static final class DiscEnchantment extends Enchantment {
        private final int maxLevel;
        private final int minBaseCost;
        private final int costPerLevel;
        private final int maxCostOffset;

        private DiscEnchantment(Rarity rarity, int maxLevel, int minBaseCost, int costPerLevel, int maxCostOffset) {
            super(rarity, THROWING_DISC_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND});
            this.maxLevel = maxLevel;
            this.minBaseCost = minBaseCost;
            this.costPerLevel = costPerLevel;
            this.maxCostOffset = maxCostOffset;
        }

        @Override
        public int getMinCost(int level) {
            return this.minBaseCost + (level - 1) * this.costPerLevel;
        }

        @Override
        public int getMaxCost(int level) {
            return this.maxCostOffset + (level - 1) * this.costPerLevel;
        }

        @Override
        public int getMaxLevel() {
            return this.maxLevel;
        }
    }
}
