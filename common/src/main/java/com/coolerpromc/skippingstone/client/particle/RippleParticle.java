package com.coolerpromc.skippingstone.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** A ring lying flat on the water that expands quickly, then slows and fades, like a real ripple. */
public class RippleParticle extends SingleQuadParticle {
    private static final FacingCameraMode FLAT_ON_WATER = (rotation, camera, partialTick) -> rotation.rotationX(-Mth.HALF_PI);
    private static final float START_RADIUS = 0.08F;
    private static final float START_ALPHA = 0.85F;

    private final float maxRadius;

    protected RippleParticle(ClientLevel level, double x, double y, double z, float strength, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.maxRadius = strength;
        this.lifetime = 14 + (int) (strength * 12);
        this.quadSize = START_RADIUS;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.setColor(0.86F, 0.94F, 1.0F);
        this.setAlpha(START_ALPHA);
    }

    @Override
    public void tick() {
        super.tick();
        float life = (float) this.age / this.lifetime;
        this.setAlpha(START_ALPHA * (1.0F - life) * (1.0F - life));
    }

    @Override
    public float getQuadSize(float partialTick) {
        float t = Math.min((this.age + partialTick) / this.lifetime, 1.0F);
        float easeOut = 1.0F - (1.0F - t) * (1.0F - t) * (1.0F - t);
        return START_RADIUS + (this.maxRadius - START_RADIUS) * easeOut;
    }

    @Override
    public FacingCameraMode getFacingCameraMode() {
        return FLAT_ON_WATER;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double strength, double unusedY, double unusedZ, RandomSource random) {
            return new RippleParticle(level, x, y, z, (float) Math.clamp(strength, 0.3, 3.0), this.sprites.get(random));
        }
    }
}
