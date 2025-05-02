package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.util.context.CommonContext;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.util.context.ContextKey;
import net.momirealms.craftengine.core.world.World;

import java.util.Optional;
import java.util.Random;

public class LootContext extends CommonContext {
    private final World world;
    private final Random randomSource;
    private final float luck;

    public LootContext(World world, float luck, Random randomSource, ContextHolder contexts) {
        super(contexts);
        this.randomSource = randomSource;
        this.world = world;
        this.luck = luck;
    }

    public Random randomSource() {
        return this.randomSource;
    }

    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        return super.contexts.getOptional(parameter);
    }

    public float luck() {
        return luck;
    }

    public World world() {
        return world;
    }
}
