package com.hp.throwingdisc.registry;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.config.DiscDefaults;
import com.hp.throwingdisc.item.DiscPlaceholderItem;
import com.hp.throwingdisc.item.ThrowingDiscItem;
import java.util.List;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Throwingdisc.MOD_ID);

    public static final DeferredItem<ThrowingDiscItem> COPPER_THROWING_DISC = ITEMS.registerItem(
        "copper_throwing_disc",
        props -> new ThrowingDiscItem(DiscDefaults.COPPER, props)
    );
    public static final DeferredItem<ThrowingDiscItem> IRON_THROWING_DISC = ITEMS.registerItem(
        "iron_throwing_disc",
        props -> new ThrowingDiscItem(DiscDefaults.IRON, props)
    );
    public static final DeferredItem<ThrowingDiscItem> GOLDEN_THROWING_DISC = ITEMS.registerItem(
        "golden_throwing_disc",
        props -> new ThrowingDiscItem(DiscDefaults.GOLDEN, props)
    );
    public static final DeferredItem<ThrowingDiscItem> DIAMOND_THROWING_DISC = ITEMS.registerItem(
        "diamond_throwing_disc",
        props -> new ThrowingDiscItem(DiscDefaults.DIAMOND, props)
    );
    public static final DeferredItem<ThrowingDiscItem> NETHERITE_THROWING_DISC = ITEMS.registerItem(
        "netherite_throwing_disc",
        props -> new ThrowingDiscItem(DiscDefaults.NETHERITE, props)
    );
    public static final DeferredItem<DiscPlaceholderItem> DISC_PLACEHOLDER = ITEMS.registerItem(
        "disc_placeholder",
        DiscPlaceholderItem::new
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
