package com.hp.throwingdisc.config;

import net.minecraft.world.item.ToolMaterial;

public enum DiscDefaults {
    COPPER("copper", ToolMaterial.COPPER, 190, 3.0, -2.2, 5.0, 0.68, 12.0, 3, 8, false),
    IRON("iron", ToolMaterial.IRON, 250, 4.0, -2.2, 6.0, 0.74, 14.0, 4, 8, false),
    GOLDEN("golden", ToolMaterial.GOLD, 64, 3.0, -1.8, 5.0, 0.92, 15.0, 5, 5, false),
    DIAMOND("diamond", ToolMaterial.DIAMOND, 1561, 5.0, -2.1, 7.5, 0.80, 16.0, 5, 7, false),
    NETHERITE("netherite", ToolMaterial.NETHERITE, 2031, 6.0, -2.0, 8.5, 0.85, 18.0, 6, 6, true);

    private final String id;
    private final ToolMaterial baseMaterial;
    private final int durability;
    private final double meleeDamageBonus;
    private final double attackSpeedBonus;
    private final double thrownDamage;
    private final double throwSpeed;
    private final double maxRange;
    private final int maxBounces;
    private final int cooldownTicks;
    private final boolean fireResistant;

    DiscDefaults(
        String id,
        ToolMaterial baseMaterial,
        int durability,
        double meleeDamageBonus,
        double attackSpeedBonus,
        double thrownDamage,
        double throwSpeed,
        double maxRange,
        int maxBounces,
        int cooldownTicks,
        boolean fireResistant
    ) {
        this.id = id;
        this.baseMaterial = baseMaterial;
        this.durability = durability;
        this.meleeDamageBonus = meleeDamageBonus;
        this.attackSpeedBonus = attackSpeedBonus;
        this.thrownDamage = thrownDamage;
        this.throwSpeed = throwSpeed;
        this.maxRange = maxRange;
        this.maxBounces = maxBounces;
        this.cooldownTicks = cooldownTicks;
        this.fireResistant = fireResistant;
    }

    public String id() {
        return this.id;
    }

    public ToolMaterial baseMaterial() {
        return this.baseMaterial;
    }

    public int durability() {
        return this.durability;
    }

    public double meleeDamageBonus() {
        return this.meleeDamageBonus;
    }

    public double attackSpeedBonus() {
        return this.attackSpeedBonus;
    }

    public double thrownDamage() {
        return this.thrownDamage;
    }

    public double throwSpeed() {
        return this.throwSpeed;
    }

    public double maxRange() {
        return this.maxRange;
    }

    public int maxBounces() {
        return this.maxBounces;
    }

    public int cooldownTicks() {
        return this.cooldownTicks;
    }

    public boolean fireResistant() {
        return this.fireResistant;
    }
}
