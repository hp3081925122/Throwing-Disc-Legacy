package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    public static final TagKey<Item> THROWING_DISCS = ItemTags.create(Throwingdisc.id("throwing_discs"));
    public static final TagKey<Item> COPPER_TOOL_MATERIALS = ItemTags.create(Throwingdisc.id("copper_tool_materials"));

    private ModTags() {
    }
}
