package com.hp.throwingdisc.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class ThrownDiscRenderState extends EntityRenderState {
    public final ItemStackRenderState discItem = new ItemStackRenderState();
    public final ItemStackRenderState cargoItem = new ItemStackRenderState();
    public float yRot;
    public float xRot;
    public float spinAngle;
    public float cargoSpinAngle;
}
