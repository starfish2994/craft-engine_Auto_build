package net.momirealms.craftengine.core.util.context;

import net.momirealms.craftengine.core.util.Key;

import java.util.UUID;

public final class PlayerParameters {
    private PlayerParameters() {}

    public static final ContextKey<Double> X = new ContextKey<>(Key.of("craftengine:player.x"));
    public static final ContextKey<Double> Y = new ContextKey<>(Key.of("craftengine:player.y"));
    public static final ContextKey<Double> Z = new ContextKey<>(Key.of("craftengine:player.z"));
    public static final ContextKey<Integer> BLOCK_X = new ContextKey<>(Key.of("craftengine:player.block_x"));
    public static final ContextKey<Integer> BLOCK_Y = new ContextKey<>(Key.of("craftengine:player.block_y"));
    public static final ContextKey<Integer> BLOCK_Z = new ContextKey<>(Key.of("craftengine:player.block_z"));
    public static final ContextKey<String> NAME = new ContextKey<>(Key.of("craftengine:player.name"));
    public static final ContextKey<UUID> UUID = new ContextKey<>(Key.of("craftengine:player.uuid"));
}
