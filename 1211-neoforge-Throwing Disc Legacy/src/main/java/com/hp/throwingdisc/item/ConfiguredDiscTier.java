package com.hp.throwingdisc.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

public final class ConfiguredDiscTier implements Tier {
    private final Tier delegate;
    private final int uses;
    private final float attackDamageBonus;

    public ConfiguredDiscTier(Tier delegate, int uses, float attackDamageBonus) {
        this.delegate = delegate;
        this.uses = uses;
        this.attackDamageBonus = attackDamageBonus;
    }

    @Override
    public int getUses() {
        return this.uses;
    }

    @Override
    public float getSpeed() {
        return this.delegate.getSpeed();
    }

    @Override
    public float getAttackDamageBonus() {
        return this.attackDamageBonus;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return this.delegate.getIncorrectBlocksForDrops();
    }

    @Override
    public int getEnchantmentValue() {
        return this.delegate.getEnchantmentValue();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return this.delegate.getRepairIngredient();
    }
}
