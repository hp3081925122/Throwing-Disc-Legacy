package com.hp.throwingdisc;

import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.item.DiscPlaceholderItem;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import com.hp.throwingdisc.registry.ModCreativeTabs;
import com.hp.throwingdisc.registry.ModEnchantments;
import com.hp.throwingdisc.registry.ModEntities;
import com.hp.throwingdisc.registry.ModItems;
import com.hp.throwingdisc.registry.ModLoot;
import com.hp.throwingdisc.util.VeinHarvestHelper;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;

@Mod(Throwingdisc.MOD_ID)
public final class Throwingdisc {
    public static final String MOD_ID = "throwingdisc";

    public Throwingdisc() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ThrowingDiscConfig.register(ModLoadingContext.get());
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModEnchantments.ENCHANTMENTS.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);

        modEventBus.addListener(Throwingdisc::onCommonSetup);
        modEventBus.addListener(ModCreativeTabs::onBuildTabContents);
        MinecraftForge.EVENT_BUS.addListener(ModLoot::onLootTableLoad);
        MinecraftForge.EVENT_BUS.addListener(DiscPlaceholderItem::onContainerClose);
        MinecraftForge.EVENT_BUS.addListener(VeinHarvestHelper::onCreativeBreakEvent);
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (RegistryObject<ThrowingDiscItem> discHolder : ModItems.discs()) {
                ThrowingDiscItem disc = discHolder.get();
                DispenserBlock.registerBehavior(disc, new AbstractProjectileDispenseBehavior() {
                    @Override
                    protected Projectile getProjectile(Level level, Position position, ItemStack stack) {
                        return disc.createProjectile(level, position, stack);
                    }

                    @Override
                    protected float getPower() {
                        return disc.dispensePower();
                    }

                    @Override
                    protected float getUncertainty() {
                        return 1.5F;
                    }
                });
            }
        });
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
