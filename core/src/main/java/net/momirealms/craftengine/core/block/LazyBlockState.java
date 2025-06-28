package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.plugin.CraftEngine;

public final class LazyBlockState {
    private final String state;
    private BlockStateWrapper packedBlockState;

    public LazyBlockState(String state) {
        this.state = state;
    }

    public BlockStateWrapper getState() {
        if (this.packedBlockState == null) {
            this.packedBlockState = CraftEngine.instance().blockManager().createPackedBlockState(state);
            if (this.packedBlockState == null) {
                CraftEngine.instance().logger().warn("Could not create block state: " + this.state);
            }
        }
        return this.packedBlockState;
    }
}