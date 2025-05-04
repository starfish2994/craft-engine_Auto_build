package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.PlayerOptionalContext;
import net.momirealms.craftengine.core.plugin.context.parameter.CommonParameters;
import net.momirealms.craftengine.core.world.World;

public class LootContext extends PlayerOptionalContext {
    private final World world;
    private final float luck;

    public LootContext(World world, float luck, ContextHolder contexts) {
        super(contexts.getOptional(CommonParameters.PLAYER).orElse(null), contexts);
        this.world = world;
        this.luck = luck;
    }

    public float luck() {
        return luck;
    }

    public World world() {
        return world;
    }
}
