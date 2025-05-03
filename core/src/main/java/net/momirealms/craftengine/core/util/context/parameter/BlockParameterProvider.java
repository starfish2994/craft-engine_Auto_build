package net.momirealms.craftengine.core.util.context.parameter;

import net.momirealms.craftengine.core.util.MCUtils;
import net.momirealms.craftengine.core.util.context.ContextKey;
import net.momirealms.craftengine.core.util.context.LazyContextParameterProvider;
import net.momirealms.craftengine.core.world.BlockInWorld;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class BlockParameterProvider implements LazyContextParameterProvider {
    private static final Map<ContextKey<?>, Function<BlockInWorld, Object>> CONTEXT_FUNCTIONS = new HashMap<>();
    static {
        CONTEXT_FUNCTIONS.put(BlockParameters.BLOCK_X, b -> MCUtils.fastFloor(b.x()));
        CONTEXT_FUNCTIONS.put(BlockParameters.BLOCK_Y, b -> MCUtils.fastFloor(b.y()));
        CONTEXT_FUNCTIONS.put(BlockParameters.BLOCK_Z, b -> MCUtils.fastFloor(b.z()));
    }

    private final BlockInWorld block;

    public BlockParameterProvider(@NotNull BlockInWorld block) {
        this.block = Objects.requireNonNull(block);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        return (Optional<T>) Optional.ofNullable(CONTEXT_FUNCTIONS.get(parameter)).map(f -> f.apply(this.block));
    }
}