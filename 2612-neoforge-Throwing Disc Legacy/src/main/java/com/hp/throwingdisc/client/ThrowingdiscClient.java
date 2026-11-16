package com.hp.throwingdisc.client;

import com.hp.throwingdisc.Throwingdisc;
import com.hp.throwingdisc.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Throwingdisc.MOD_ID, dist = Dist.CLIENT)
public final class ThrowingdiscClient {
    public ThrowingdiscClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ThrowingdiscClient::registerRenderers);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THROWN_DISC.get(), ThrownDiscRenderer::new);
    }
}
