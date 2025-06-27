package net.momirealms.craftengine.core.block;

public interface CustomBlockStateHolder {

    ImmutableBlockState customBlockState();

    void setCustomBlockState(ImmutableBlockState state);
}
