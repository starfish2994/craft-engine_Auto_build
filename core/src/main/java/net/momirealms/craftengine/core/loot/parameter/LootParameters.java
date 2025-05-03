package net.momirealms.craftengine.core.loot.parameter;

import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.context.ContextKey;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;

public final class LootParameters {
    private LootParameters() {}

    public static final ContextKey<Vec3d> LOCATION = ContextKey.of("location");
    public static final ContextKey<World> WORLD = ContextKey.of("world");
    public static final ContextKey<Entity> ENTITY = ContextKey.of("entity");
    public static final ContextKey<Boolean> FALLING_BLOCK = ContextKey.of("falling_block");
    public static final ContextKey<Float> EXPLOSION_RADIUS = ContextKey.of("explosion_radius");
    public static final ContextKey<Player> PLAYER = ContextKey.of("player");
    public static final ContextKey<Item<?>> TOOL = ContextKey.of("tool");
    public static final ContextKey<ImmutableBlockState> BLOCK_STATE = ContextKey.of("block_state");
}
