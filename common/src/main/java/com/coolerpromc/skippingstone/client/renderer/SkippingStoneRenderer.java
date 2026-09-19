package com.coolerpromc.skippingstone.client.renderer;

import com.coolerpromc.skippingstone.entity.custom.SkippingStoneEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

/**
 * Like vanilla's ThrownItemRenderer, but instead of a camera-facing sprite the stone lies flat, faces its direction of
 * travel and spins like a real skipped stone: faster when it moves faster, nose up while climbing and down while
 * falling, and it tips edge-down and flutters once it sinks.
 */
public class SkippingStoneRenderer extends EntityRenderer<SkippingStoneEntity, SkippingStoneRenderer.State> {
    private static final float BASE_TILT_DEGREES = 10.0F;
    private static final float ARC_TILT_FACTOR = 0.6F;
    private static final float MAX_ARC_TILT_DEGREES = 25.0F;
    private static final float SCALE = 1.25F;

    private final ItemModelResolver itemModelResolver;

    public SkippingStoneRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    public static class State extends ThrownItemRenderState {
        public float heading;
        public float tilt;
        public float spin;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(SkippingStoneEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);

        Vec3 motion = entity.getDeltaMovement();
        if (motion.horizontalDistanceSqr() > 1.0E-6) {
            state.heading = (float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG);
        } else {
            state.heading = entity.getYRot();
        }
        float arcDegrees = (float) (Mth.atan2(motion.y, Math.max(motion.horizontalDistance(), 1.0E-4)) * Mth.RAD_TO_DEG);
        float arcTilt = entity.isSinking() ? 0.0F : Mth.clamp(arcDegrees * ARC_TILT_FACTOR, -MAX_ARC_TILT_DEGREES, MAX_ARC_TILT_DEGREES);
        state.tilt = BASE_TILT_DEGREES + arcTilt + entity.getSinkTilt(partialTick);
        state.spin = entity.getSpin(partialTick);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0.0F, state.boundingBoxHeight / 2.0F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.heading));
        // Lie flat with the leading edge slightly raised, the way a stone planes across the surface
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - state.tilt));
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.spin));
        poseStack.scale(SCALE, SCALE, SCALE);
        // The GROUND transform lifts items by 2px; undo it so the stone is centred on its hitbox
        poseStack.translate(0.0F, -0.125F, 0.0F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
