package com.hp.throwingdisc;

import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.item.DiscPlaceholderItem;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import com.hp.throwingdisc.registry.ModCreativeTabs;
import com.hp.throwingdisc.registry.ModEntities;
import com.hp.throwingdisc.registry.ModItems;
import com.hp.throwingdisc.registry.ModLoot;
import com.hp.throwingdisc.util.VeinHarvestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredItem;

@Mod(Throwingdisc.MOD_ID)
public final class Throwingdisc {
    public static final String MOD_ID = "throwingdisc";

    public Throwingdisc(IEventBus modEventBus, ModContainer modContainer) {
        ThrowingDiscConfig.register(modContainer);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        modEventBus.addListener(Throwingdisc::onCommonSetup);
        modEventBus.addListener(ModCreativeTabs::onBuildTabContents);
        NeoForge.EVENT_BUS.addListener(ModLoot::onLootTableLoad);
        NeoForge.EVENT_BUS.addListener(DiscPlaceholderItem::onContainerClose);
        NeoForge.EVENT_BUS.addListener(VeinHarvestHelper::onCreativeBreakEvent);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (DeferredItem<ThrowingDiscItem> disc : ModItems.discs()) {
                DispenserBlock.registerProjectileBehavior(disc.get());
            }
        });
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
