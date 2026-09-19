package com.coolerpromc.skippingstone.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * What a shoreline block becomes once its skipping stone has been picked out: the same material, two pixels
 * lower, so depleted spots are visible at a glance. Not listed as a pickup source, so it cannot be farmed again.
 */
public class SiftedBlock extends Block {
    public static final MapCodec<SiftedBlock> CODEC = simpleCodec(SiftedBlock::new);
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 14.0);

    public SiftedBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<SiftedBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }
}
