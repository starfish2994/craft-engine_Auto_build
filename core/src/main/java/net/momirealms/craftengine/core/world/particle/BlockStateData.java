package net.momirealms.craftengine.core.world.particle;

import net.momirealms.craftengine.core.block.BlockStateWrapper;
import net.momirealms.craftengine.core.block.LazyBlockState;

public class BlockStateData implements ParticleData {
    private final LazyBlockState blockState;

    public BlockStateData(LazyBlockState blockState) {
        this.blockState = blockState;
    }

    public BlockStateWrapper blockState() {
        return blockState.getState();
    }
}
