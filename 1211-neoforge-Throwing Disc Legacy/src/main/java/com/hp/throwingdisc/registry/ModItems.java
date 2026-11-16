package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.config.DiscDefaults;
import com.hp.throwingdisc.item.DiscPlaceholderItem;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import java.util.List;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Throwingdisc.MOD_ID);

    public static final DeferredItem<ThrowingDiscItem> COPPER_THROWING_DISC = ITEMS.register(
        "copper_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.COPPER, new Item.Properties())
    );
    public static final DeferredItem<ThrowingDiscItem> IRON_THROWING_DISC = ITEMS.register(
        "iron_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.IRON, new Item.Properties())
    );
    public static final DeferredItem<ThrowingDiscItem> GOLDEN_THROWING_DISC = ITEMS.register(
        "golden_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.GOLDEN, new Item.Properties())
    );
    public static final DeferredItem<ThrowingDiscItem> DIAMOND_THROWING_DISC = ITEMS.register(
        "diamond_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.DIAMOND, new Item.Properties())
    );
    public static final DeferredItem<ThrowingDiscItem> NETHERITE_THROWING_DISC = ITEMS.register(
        "netherite_throwing_disc",
        () -> new ThrowingDiscItem(DiscDefaults.NETHERITE, new Item.Properties())
    );
    public static final DeferredItem<DiscPlaceholderItem> DISC_PLACEHOLDER = ITEMS.register(
        "disc_placeholder",
        () -> new DiscPlaceholderItem(new Item.Properties())
    );

    private ModItems() {
    }

    public static List<DeferredItem<ThrowingDiscItem>> discs() {
        return List.of(
            COPPER_THROWING_DISC,
            IRON_THROWING_DISC,
            GOLDEN_THROWING_DISC,
            DIAMOND_THROWING_DISC,
            NETHERITE_THROWING_DISC
        );
    }
}
