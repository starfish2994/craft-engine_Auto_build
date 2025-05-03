package net.momirealms.craftengine.core.util.context.parameter;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.context.ContextKey;

import java.util.UUID;

public final class BlockParameters {
    private BlockParameters() {}

    public static final ContextKey<Double> X = new ContextKey<>(Key.of("craftengine:block.x"));
    public static final ContextKey<Double> Y = new ContextKey<>(Key.of("craftengine:block.y"));
    public static final ContextKey<Double> Z = new ContextKey<>(Key.of("craftengine:block.z"));
    public static final ContextKey<Integer> BLOCK_X = new ContextKey<>(Key.of("craftengine:block.block_x"));
    public static final ContextKey<Integer> BLOCK_Y = new ContextKey<>(Key.of("craftengine:block.block_y"));
    public static final ContextKey<Integer> BLOCK_Z = new ContextKey<>(Key.of("craftengine:block.block_z"));
    public static final ContextKey<ImmutableBlockState> BLOCK_STATE = new ContextKey<>(Key.of("craftengine:block.state"));
    public static final ContextKey<Key> BLOCK_OWNER = new ContextKey<>(Key.of("craftengine:block.owner"));
}
