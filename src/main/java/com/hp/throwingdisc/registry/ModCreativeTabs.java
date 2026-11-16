package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Throwingdisc.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THROWING_DISC_TAB = TABS.register(
        Throwingdisc.MOD_ID,
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Throwingdisc.MOD_ID))
            .icon(() -> new ItemStack(ModItems.DIAMOND_THROWING_DISC.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((parameters, output) -> {
                for (DeferredItem<ThrowingDiscItem> disc : ModItems.discs()) {
                    output.accept(disc.get());
                }

                parameters.holders().lookup(Registries.ENCHANTMENT).ifPresent(lookup -> {
                    for (ResourceKey<Enchantment> key : ModEnchantments.all()) {
                        lookup.get(key).ifPresent(holder -> {
                            for (int level = holder.value().getMinLevel(); level <= holder.value().getMaxLevel(); level++) {
                                output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(holder, level)));
                            }
                        });
                    }
                });
            })
            .build()
    );

    private ModCreativeTabs() {
    }

    public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            ItemStack anchor = new ItemStack(Items.TRIDENT);
            for (DeferredItem<ThrowingDiscItem> disc : ModItems.discs()) {
                ItemStack discStack = new ItemStack(disc.get());
                if (event.getParentEntries().contains(anchor)) {
                    event.insertAfter(anchor, discStack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
                } else {
                    event.accept(discStack);
                }
                anchor = discStack;
            }
        }
    }
}
