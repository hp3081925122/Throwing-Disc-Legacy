package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.config.DiscDefaults;
import com.hp.throwingdisc.item.DiscPlaceholderItem;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Throwingdisc.MOD_ID);

    public static final RegistryObject<ThrowingDiscItem> COPPER_THROWING_DISC = ITEMS.register(
        "copper_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.COPPER, new Item.Properties())
    );
    public static final RegistryObject<ThrowingDiscItem> IRON_THROWING_DISC = ITEMS.register(
        "iron_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.IRON, new Item.Properties())
    );
    public static final RegistryObject<ThrowingDiscItem> GOLDEN_THROWING_DISC = ITEMS.register(
        "golden_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.GOLDEN, new Item.Properties())
    );
    public static final RegistryObject<ThrowingDiscItem> DIAMOND_THROWING_DISC = ITEMS.register(
        "diamond_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.DIAMOND, new Item.Properties())
    );
    public static final RegistryObject<ThrowingDiscItem> NETHERITE_THROWING_DISC = ITEMS.register(
        "netherite_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.NETHERITE, new Item.Properties())
    );
    public static final RegistryObject<DiscPlaceholderItem> DISC_PLACEHOLDER = ITEMS.register(
        "disc_placeholder",
        () -> new DiscPlaceholderItem(new Item.Properties())
    );

    private ModItems() {
    }

    public static List<RegistryObject<ThrowingDiscItem>> discs() {
        return List.of(
            COPPER_THROWING_DISC,
            IRON_THROWING_DISC,
            GOLDEN_THROWING_DISC,
            DIAMOND_THROWING_DISC,
            NETHERITE_THROWING_DISC
        );
    }
}
