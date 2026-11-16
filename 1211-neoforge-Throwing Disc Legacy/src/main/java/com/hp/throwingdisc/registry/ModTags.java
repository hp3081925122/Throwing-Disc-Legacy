package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static final TagKey<Item> THROWING_DISCS = TagKey.create(Registries.ITEM, Throwingdisc.id("throwing_discs"));
    public static final TagKey<Item> COPPER_TOOL_MATERIALS = TagKey.create(Registries.ITEM, Throwingdisc.id("copper_tool_materials"));
    public static final TagKey<Item> ENCHANTABLE_THROWING_DISC = TagKey.create(Registries.ITEM, Throwingdisc.id("enchantable/throwing_disc"));

    private ModTags() {
    }
}
