package com.hp.throwingdisc.item;

import com.hp.throwingdisc.registry.ModTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public enum DiscTiers implements Tier {
    COPPER;

    @Override
    public int getUses() {
        return 190;
    }

    @Override
    public float getSpeed() {
        return 5.0F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 3.0F;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_STONE_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return 13;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModTags.COPPER_TOOL_MATERIALS);
    }
}
