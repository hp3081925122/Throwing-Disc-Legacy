package com.hp.throwingdisc.client;

import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.entity.ThrownDiscEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrownDiscRenderer extends EntityRenderer<ThrownDiscEntity, ThrownDiscRenderState> {
    private final ItemModelResolver itemModelResolver;

    public ThrownDiscRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.shadowRadius = 0.2F;
        this.shadowStrength = 0.5F;
    }

    @Override
    public ThrownDiscRenderState createRenderState() {
        return new ThrownDiscRenderState();
    }

    @Override
    public void extractRenderState(ThrownDiscEntity entity, ThrownDiscRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        ItemStack discStack = entity.getItem();
        if (!discStack.isEmpty()) {
            this.itemModelResolver.updateForNonLiving(state.discItem, discStack, ItemDisplayContext.FIXED, entity);
        } else {
            state.discItem.clear();
        }

        ItemStack cargoStack = entity.getCargoPreview();
        if (!cargoStack.isEmpty()) {
            this.itemModelResolver.updateForNonLiving(state.cargoItem, cargoStack, ItemDisplayContext.GROUND, entity);
        } else {
            state.cargoItem.clear();
        }

        state.yRot = entity.getYRot(partialTick);
        state.xRot = entity.getXRot(partialTick);
        float spinRate = ThrowingDiscConfig.CLIENT_SPEC.isLoaded()
            ? ThrowingDiscConfig.CLIENT.spinDegreesPerTick.get().floatValue()
            : 38.0F;
        state.spinAngle = ((entity.tickCount + partialTick) * spinRate) % 360.0F;
        state.cargoSpinAngle = ((entity.tickCount + partialTick) * 12.0F) % 360.0F;
    }

    @Override
    public void submit(ThrownDiscRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (!state.discItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.1F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(state.spinAngle));
            poseStack.scale(0.92F, 0.92F, 0.92F);
            state.discItem.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }

        if (!state.cargoItem.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.24F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(state.cargoSpinAngle));
            poseStack.scale(0.42F, 0.42F, 0.42F);
            state.cargoItem.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }

        super.submit(state, poseStack, submitNodeCollector, camera);
    }
}
