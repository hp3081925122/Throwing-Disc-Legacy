package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(
        Registries.CREATIVE_MODE_TAB,
        Throwingdisc.MOD_ID
    );

    public static final RegistryObject<CreativeModeTab> THROWING_DISC_TAB = TABS.register(
        Throwingdisc.MOD_ID,
        () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + Throwingdisc.MOD_ID))
            .icon(() -> new ItemStack(ModItems.DIAMOND_THROWING_DISC.get()))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .displayItems((parameters, output) -> {
                for (RegistryObject<ThrowingDiscItem> disc : ModItems.discs()) {
                    output.accept(disc.get());
                }
                for (RegistryObject<Enchantment> holder : ModEnchantments.all()) {
                    Enchantment ench = holder.get();
                    for (int level = ench.getMinLevel(); level <= ench.getMaxLevel(); level++) {
                        output.accept(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(ench, level)));
                    }
                }
            })
            .build()
    );

    private ModCreativeTabs() {
    }

    public static void onBuildTabContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            for (RegistryObject<ThrowingDiscItem> disc : ModItems.discs()) {
                event.accept(disc.get());
            }
        }
    }
}
