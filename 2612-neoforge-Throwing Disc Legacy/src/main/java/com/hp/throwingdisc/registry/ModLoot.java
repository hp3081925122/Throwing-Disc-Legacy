package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.config.ThrowingDiscConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.event.LootTableLoadEvent;

public final class ModLoot {
    private static final String POOL_NAME = "throwingdisc_bonus_pool";

    private ModLoot() {
    }

    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!ThrowingDiscConfig.COMMON.enableLootInjection.get()) {
            return;
        }

        Identifier id = event.getName();
        if (!"minecraft".equals(id.getNamespace())) {
            return;
        }

        LootTable table = event.getTable();
        if (table.getPool(POOL_NAME) != null) {
            return;
        }

        HolderLookup.Provider registries = event.getRegistries();
        switch (id.getPath()) {
            case "chests/trial_chambers/entrance", "chests/trial_chambers/corridor" -> {
                if (ThrowingDiscConfig.COMMON.lootTrialChambers.get()) {
                    table.addPool(singleDiscPool(ModItems.COPPER_THROWING_DISC.get(), 0.22F, registries, false));
                }
            }
            case "chests/trial_chambers/reward_common", "chests/trial_chambers/reward_rare" -> {
                if (ThrowingDiscConfig.COMMON.lootTrialChambers.get()) {
                    LootPool pool = LootPool.lootPool()
                        .name(POOL_NAME)
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.25F))
                        .add(LootItem.lootTableItem(ModItems.COPPER_THROWING_DISC.get())
                            .setWeight(3)
                            .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries)))
                        .add(LootItem.lootTableItem(ModItems.IRON_THROWING_DISC.get())
                            .setWeight(2)
                            .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(12.0F, 24.0F))))
                        .build();
                    table.addPool(pool);
                }
            }
            case "chests/village/village_weaponsmith" -> {
                if (ThrowingDiscConfig.COMMON.lootVillageWeaponsmith.get()) {
                    LootPool pool = LootPool.lootPool()
                        .name(POOL_NAME)
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.30F))
                        .add(LootItem.lootTableItem(ModItems.COPPER_THROWING_DISC.get()).setWeight(3))
                        .add(LootItem.lootTableItem(ModItems.IRON_THROWING_DISC.get()).setWeight(2))
                        .build();
                    table.addPool(pool);
                }
            }
            case "chests/buried_treasure" -> {
                if (ThrowingDiscConfig.COMMON.lootBuriedTreasure.get()) {
                    table.addPool(singleDiscPool(ModItems.IRON_THROWING_DISC.get(), 0.35F, registries, true));
                }
            }
            case "chests/bastion_treasure" -> {
                if (ThrowingDiscConfig.COMMON.lootBastionTreasure.get()) {
                    LootPool pool = LootPool.lootPool()
                        .name(POOL_NAME)
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.35F))
                        .add(LootItem.lootTableItem(ModItems.GOLDEN_THROWING_DISC.get())
                            .setWeight(3)
                            .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(18.0F, 32.0F))))
                        .add(LootItem.lootTableItem(ModItems.DIAMOND_THROWING_DISC.get())
                            .setWeight(2)
                            .apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries)))
                        .build();
                    table.addPool(pool);
                }
            }
            case "chests/end_city_treasure" -> {
                if (ThrowingDiscConfig.COMMON.lootEndCityTreasure.get()) {
                    LootPool pool = LootPool.lootPool()
                        .name(POOL_NAME)
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(LootItemRandomChanceCondition.randomChance(0.28F))
                        .add(LootItem.lootTableItem(ModItems.DIAMOND_THROWING_DISC.get())
                            .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(20.0F, 39.0F))))
                        .build();
                    table.addPool(pool);
                }
            }
            default -> {
            }
        }
    }

    private static LootPool singleDiscPool(Item item, float chance, HolderLookup.Provider registries, boolean enchantRandomly) {
        var entry = LootItem.lootTableItem(item);
        if (enchantRandomly) {
            entry.apply(EnchantRandomlyFunction.randomApplicableEnchantment(registries));
        }
        return LootPool.lootPool()
            .name(POOL_NAME)
            .setRolls(ConstantValue.exactly(1.0F))
            .when(LootItemRandomChanceCondition.randomChance(chance))
            .add(entry)
            .build();
    }
}
