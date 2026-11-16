package com.hp.throwingdisc.client;

import com.hp.throwingdisc.config.ThrowingDiscConfig;
import com.hp.throwingdisc.entity.ThrownDiscEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ThrownDiscRenderer extends EntityRenderer<ThrownDiscEntity> {
    private final ItemRenderer itemRenderer;

    public ThrownDiscRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.2F;
        this.shadowStrength = 0.5F;
    }

    @Override
    public void render(
        ThrownDiscEntity entity,
        float entityYaw,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight
    ) {
        ItemStack stack = entity.getItem();
        if (stack.isEmpty()) {
            return;
        }

        float yaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        float spinRate = ThrowingDiscConfig.CLIENT_SPEC.isLoaded()
            ? ThrowingDiscConfig.CLIENT.spinDegreesPerTick.get().floatValue()
            : 38.0F;
        float spinAngle = ((entity.tickCount + partialTick) * spinRate) % 360.0F;

        poseStack.pushPose();
        poseStack.translate(0.0F, 0.1F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(spinAngle));
        poseStack.scale(0.92F, 0.92F, 0.92F);

        this.itemRenderer.renderStatic(
            stack,
            ItemDisplayContext.FIXED,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            buffer,
            entity.level(),
            entity.getId()
        );
        poseStack.popPose();

        ItemStack cargo = entity.getCargoPreview();
        if (!cargo.isEmpty()) {
            poseStack.pushPose();
            poseStack.translate(0.0F, 0.24F, 0.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(((entity.tickCount + partialTick) * 12.0F) % 360.0F));
            poseStack.scale(0.42F, 0.42F, 0.42F);
            this.itemRenderer.renderStatic(
                cargo,
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId() + 1
            );
            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation getTextureLocation(ThrownDiscEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
