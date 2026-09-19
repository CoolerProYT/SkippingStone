/*
 * Water-skipping, entity-hit, celebration-firework and skip-counter behaviour adapted from
 * Hezaerd/Skipping-Stones (RockEntity), https://github.com/Hezaerd/Skipping-Stones
 * MIT License, Copyright (c) 2025 Hezaerd. See META-INF/licenses/Hezaerd-Skipping-Stones-LICENSE.txt.
 */
package com.coolerpromc.skippingstone.entity.custom;

import com.coolerpromc.skippingstone.entity.ModEntities;
import com.coolerpromc.skippingstone.item.ModItems;
import com.coolerpromc.skippingstone.particle.ModParticles;
import com.coolerpromc.skippingstone.stats.SkipRecords;
import com.coolerpromc.skippingstone.throwing.ThrowHandler;
import com.coolerpromc.skippingstone.throwing.logic.HopPhysics;
import com.coolerpromc.skippingstone.throwing.logic.ThrowResult;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * A thrown skipping stone. Each time it touches water it bounces into the next hop of its {@link ThrowResult}, with a
 * launch velocity from {@link HopPhysics} so the hop covers exactly the calculated distance. Once the hops run out it
 * skims along the surface for a moment, then sinks. A red release sinks on first contact.
 *
 * <p>The hop sequence is geometric ({@code first * decay^i}), so it is synced once at spawn as three values. The client
 * replays the same bounces locally, which keeps the skipping smooth instead of waiting on server corrections.
 */
public class SkippingStoneEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Float> DATA_FIRST_HOP = SynchedEntityData.defineId(SkippingStoneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_HOP_DECAY = SynchedEntityData.defineId(SkippingStoneEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_HOP_COUNT = SynchedEntityData.defineId(SkippingStoneEntity.class, EntityDataSerializers.INT);

    private static final float HIT_DAMAGE = 1.0F;
    private static final int MAX_LIFETIME_TICKS = 600;
    private static final int SINK_TICKS = 30;
    private static final double SURFACE_OFFSET = 0.02;
    private static final int BOUNCE_COOLDOWN_TICKS = 1;

    // Final skim: the stone slides just below the surface, slowed by water drag, until it is too slow to plane
    private static final double SKID_DEPTH = 0.06;
    private static final double SKID_MIN_SPEED = 0.08;
    private static final int SKID_MAX_TICKS = 14;

    // Visual spin (client): degrees per tick per block/tick of horizontal speed
    private static final float SPIN_PER_SPEED = 90.0F;
    private static final float MIN_FLIGHT_SPIN = 6.0F;
    private static final float SINK_TILT_DEGREES = 70.0F;

    private enum Phase { FLYING, SKIDDING, SINKING }

    private Phase phase = Phase.FLYING;
    private int hopsDone;
    private int lastBounceTick = -BOUNCE_COOLDOWN_TICKS - 1;
    private int skidTicks;

    // Client visuals
    private float spin;
    private float oSpin;
    private float sinkTilt;
    private float oSinkTilt;

    // Server only
    private boolean awardsStats;
    private double distanceTravelled;
    private Vec3 lastContact;
    private int sinkingTicks;
    private boolean finished;
    private SkipRecords.PlayerRecord recordBeforeThrow;
    private boolean recordFireworkLaunched;

    public SkippingStoneEntity(EntityType<? extends SkippingStoneEntity> type, Level level) {
        super(type, level);
    }

    public SkippingStoneEntity(Level level, LivingEntity owner, ItemStack stack, ThrowResult result) {
        super(ModEntities.SKIPPING_STONE.get(), owner, level, stack);
        this.applyResult(result);
        if (owner instanceof ServerPlayer player) {
            this.recordBeforeThrow = ThrowHandler.currentRecord(player);
        }
    }

    public SkippingStoneEntity(Level level, double x, double y, double z, ItemStack stack, ThrowResult result) {
        super(ModEntities.SKIPPING_STONE.get(), x, y, z, level, stack);
        this.applyResult(result);
    }

    private void applyResult(ThrowResult result) {
        List<Double> hops = result.skipDistances();
        this.entityData.set(DATA_HOP_COUNT, hops.size());
        if (!hops.isEmpty()) {
            this.entityData.set(DATA_FIRST_HOP, hops.getFirst().floatValue());
            this.entityData.set(DATA_HOP_DECAY, hops.size() > 1 ? (float) (hops.get(1) / hops.getFirst()) : 0.0F);
        }
        this.awardsStats = result.awardsStats();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SKIPPING_STONE.get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FIRST_HOP, 0.0F);
        builder.define(DATA_HOP_DECAY, 0.0F);
        builder.define(DATA_HOP_COUNT, 0);
    }

    /** The hop plan is not saved, so a reloaded stone would sink with its stats lost. Drop it on unload instead. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    public boolean isSinking() {
        return this.phase == Phase.SINKING;
    }

    public int getSkipsPerformed() {
        return this.hopsDone;
    }

    /** Horizontal distance between the first and the latest water contact. Server only. */
    public double getDistanceTravelled() {
        return this.distanceTravelled;
    }

    public boolean hasLaunchedRecordFirework() {
        return this.recordFireworkLaunched;
    }

    public float getSpin(float partialTick) {
        return Mth.lerp(partialTick, this.oSpin, this.spin);
    }

    public float getSinkTilt(float partialTick) {
        return Mth.lerp(partialTick, this.oSinkTilt, this.sinkTilt);
    }

    private double hopDistance(int index) {
        return this.entityData.get(DATA_FIRST_HOP) * Math.pow(this.entityData.get(DATA_HOP_DECAY), index);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }

        switch (this.phase) {
            case FLYING -> {
                if (this.isInWater() && this.canBounce()) {
                    this.onWaterContact();
                }
            }
            case SKIDDING -> this.tickSkid();
            case SINKING -> {
            }
        }

        if (this.level().isClientSide()) {
            this.tickVisuals();
        } else if (this.level() instanceof ServerLevel serverLevel) {
            if (this.tickCount > MAX_LIFETIME_TICKS || this.phase == Phase.SINKING && ++this.sinkingTicks > SINK_TICKS) {
                this.discard();
            } else if (this.phase == Phase.SINKING && this.sinkingTicks % 4 == 0) {
                serverLevel.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY(), this.getZ(), 2, 0.05, 0.05, 0.05, 0.02);
            }
        }
    }

    private void tickVisuals() {
        this.oSpin = this.spin;
        this.oSinkTilt = this.sinkTilt;
        double speed = this.getDeltaMovement().horizontalDistance();
        switch (this.phase) {
            case FLYING -> this.spin += Math.max(MIN_FLIGHT_SPIN, (float) speed * SPIN_PER_SPEED);
            case SKIDDING -> this.spin += (float) speed * SPIN_PER_SPEED;
            // Tips edge-down and flutters as it sinks
            case SINKING -> {
                this.sinkTilt += (SINK_TILT_DEGREES - this.sinkTilt) * 0.15F;
                this.spin += 4.0F * Mth.sin(this.tickCount * 0.5F);
            }
        }
    }

    private boolean canBounce() {
        return this.tickCount - this.lastBounceTick > BOUNCE_COOLDOWN_TICKS;
    }

    private void onWaterContact() {
        double surface = this.waterSurfaceY();
        boolean skips = this.hopsDone < this.entityData.get(DATA_HOP_COUNT);

        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 contact = new Vec3(this.getX(), surface, this.getZ());
            if (this.lastContact != null) {
                this.distanceTravelled += contact.subtract(this.lastContact).horizontalDistance();
                this.checkDistanceRecord(serverLevel);
            }
            this.lastContact = contact;
        }

        if (!skips) {
            this.endSkipping(surface);
            return;
        }

        double distance = this.hopDistance(this.hopsDone);
        this.bounce(distance, surface);
        this.hopsDone++;
        if (this.level() instanceof ServerLevel serverLevel) {
            this.onSkip(serverLevel, surface, distance);
        }
    }

    private void bounce(double distance, double surface) {
        HopPhysics.Hop hop = HopPhysics.plan(distance);
        Vec3 direction = this.horizontalDirection();
        this.setPos(this.getX(), surface + SURFACE_OFFSET, this.getZ());
        this.setDeltaMovement(direction.x * hop.horizontalSpeed(), hop.verticalSpeed(), direction.z * hop.horizontalSpeed());
        // Otherwise next tick still sees the stale in-water flag and applies water drag, shortening the hop
        this.updateFluidInteraction();
        this.needsSync = true;
        this.lastBounceTick = this.tickCount;
    }

    private Vec3 horizontalDirection() {
        Vec3 direction = this.getDeltaMovement().horizontal();
        if (direction.lengthSqr() < 1.0E-6) {
            direction = Vec3.directionFromRotation(0, this.getYRot());
        }
        return direction.normalize();
    }

    private void onSkip(ServerLevel level, double surface, double distance) {
        // 1 for the first (biggest) skip, shrinking towards 0 for the last little ones
        float energy = (float) Math.clamp(distance / this.hopDistance(0), 0.0, 1.0);
        this.playSound(SoundEvents.PLAYER_SPLASH, 0.3F + 0.5F * energy, 1.6F - 0.6F * energy + this.random.nextFloat() * 0.1F);
        level.sendParticles(ParticleTypes.SPLASH, this.getX(), surface, this.getZ(), 4 + (int) (14 * energy), 0.1 + 0.1 * energy, 0.0, 0.1 + 0.1 * energy, 0.05 + 0.1 * energy);
        level.sendParticles(ParticleTypes.FISHING, this.getX(), surface, this.getZ(), 2 + (int) (4 * energy), 0.08, 0.0, 0.08, 0.04);
        this.sendRipple(level, surface, 0.45 + 0.13 * distance);

        if (this.getOwner() instanceof ServerPlayer player) {
            player.sendOverlayMessage(Component.literal(String.valueOf(this.hopsDone)));
        }

        if (this.awardsStats && this.recordBeforeThrow != null && this.recordBeforeThrow.bestSkips() > 0 && this.hopsDone > this.recordBeforeThrow.bestSkips()) {
            this.launchRecordFirework(level);
        }
    }

    private void checkDistanceRecord(ServerLevel level) {
        if (this.awardsStats && this.recordBeforeThrow != null && this.recordBeforeThrow.bestDistance() > 0 && this.distanceTravelled > this.recordBeforeThrow.bestDistance()) {
            this.launchRecordFirework(level);
        }
    }

    private void sendRipple(ServerLevel level, double surface, double strength) {
        // Count 0 makes the x "speed" arrive exactly as given, which the ripple uses as its size
        level.sendParticles(ModParticles.RIPPLE.get(), this.getX(), surface + 0.01, this.getZ(), 0, strength, 0.0, 0.0, 1.0);
    }

    private void endSkipping(double surface) {
        if (this.hopsDone > 0 && this.getDeltaMovement().horizontalDistance() > SKID_MIN_SPEED) {
            this.phase = Phase.SKIDDING;
            this.skidTicks = 0;
            this.keepOnSurface(surface);
            if (this.level() instanceof ServerLevel serverLevel) {
                this.sendRipple(serverLevel, surface, 0.4);
            }
        } else {
            this.startSinking();
        }
    }

    private void tickSkid() {
        double surface = this.waterSurfaceY();
        this.keepOnSurface(surface);
        this.skidTicks++;

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), surface, this.getZ(), 2, 0.05, 0.0, 0.05, 0.03);
            if (this.skidTicks % 3 == 0) {
                this.sendRipple(serverLevel, surface, 0.3);
                this.playSound(SoundEvents.PLAYER_SPLASH, 0.15F, 1.8F + this.random.nextFloat() * 0.2F);
            }
        }

        if (this.getDeltaMovement().horizontalDistance() < SKID_MIN_SPEED / 2 || this.skidTicks >= SKID_MAX_TICKS) {
            this.startSinking();
        }
    }

    private void keepOnSurface(double surface) {
        // Just under the surface so vanilla's water drag slows the slide
        this.setPos(this.getX(), surface - SKID_DEPTH, this.getZ());
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x, 0.0, motion.z);
    }

    private void startSinking() {
        this.phase = Phase.SINKING;
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.2, 0.0, 0.2).add(0, -0.05, 0));
        if (this.level() instanceof ServerLevel serverLevel) {
            this.needsSync = true;
            this.playSound(SoundEvents.GENERIC_SPLASH, 0.5F, 1.3F + this.random.nextFloat() * 0.3F);
            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY(), this.getZ(), 6, 0.1, 0.0, 0.1, 0.05);
            this.sendRipple(serverLevel, this.waterSurfaceY(), 0.6);
            this.finish();
        }
    }

    private double waterSurfaceY() {
        BlockPos pos = this.blockPosition();
        while (this.level().getFluidState(pos.above()).is(FluidTags.WATER)) {
            pos = pos.above();
        }
        FluidState fluid = this.level().getFluidState(pos);
        return fluid.is(FluidTags.WATER) ? pos.getY() + fluid.getHeight(this.level(), pos) : this.getY();
    }

    /** Hitting land or a mob ends the throw; the stone drops so a missed throw does not cost it. */
    @Override
    protected void onHit(HitResult hitResult) {
        if (hitResult instanceof BlockHitResult blockHit) {
            // Blocks holding water (kelp, seagrass, waterlogged blocks) should not stop a skip
            if (!this.level().getFluidState(blockHit.getBlockPos()).isEmpty()) {
                return;
            }
            // In shallow water a fast stone can cross the surface and reach the bed in one tick; that is still a skip
            if (this.phase == Phase.FLYING && blockHit.getDirection() == Direction.UP && this.level().getFluidState(blockHit.getBlockPos().above()).is(FluidTags.WATER)) {
                if (this.canBounce()) {
                    this.onWaterContact();
                }
                return;
            }
        }
        super.onHit(hitResult);
        if (this.level() instanceof ServerLevel serverLevel && !this.isRemoved()) {
            this.playSound(SoundEvents.STONE_HIT, 1.0F, 1.2F);
            if (this.phase != Phase.SINKING) {
                this.spawnAtLocation(serverLevel, this.getItem());
            }
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel serverLevel) {
            result.getEntity().hurtServer(serverLevel, this.damageSources().thrown(this, this.getOwner()), HIT_DAMAGE);
        }
    }

    /** Once per throw, only when the owner beats a record they already had. */
    private void launchRecordFirework(ServerLevel level) {
        if (this.recordFireworkLaunched) {
            return;
        }
        this.recordFireworkLaunched = true;

        DyeColor[] colors = DyeColor.values();
        FireworkExplosion.Shape[] shapes = FireworkExplosion.Shape.values();
        FireworkExplosion explosion = new FireworkExplosion(
            shapes[this.random.nextInt(shapes.length)],
            IntList.of(colors[this.random.nextInt(colors.length)].getFireworkColor(), colors[this.random.nextInt(colors.length)].getFireworkColor()),
            IntList.of(),
            this.random.nextBoolean(),
            this.random.nextBoolean()
        );

        ItemStack firework = new ItemStack(Items.FIREWORK_ROCKET);
        firework.set(DataComponents.FIREWORKS, new Fireworks(1 + this.random.nextInt(2), List.of(explosion)));
        Projectile.spawnProjectile(new FireworkRocketEntity(level, firework, this.getX(), this.getY() + 2, this.getZ(), true), level, firework);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!this.level().isClientSide()) {
            this.finish();
        }
        super.remove(reason);
    }

    private void finish() {
        if (this.finished) {
            return;
        }
        this.finished = true;
        // Skip and distance records are both checked at each water contact, so any record firework has already launched
        if (this.getOwner() instanceof ServerPlayer player) {
            ThrowHandler.onThrowFinished(player, this.hopsDone, this.distanceTravelled, this.awardsStats && this.hopsDone > 0);
        }
    }
}
