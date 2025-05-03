package net.momirealms.craftengine.core.loot;

import net.momirealms.craftengine.core.util.context.AbstractCommonContext;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.world.World;

import java.util.Random;

public class LootContext extends AbstractCommonContext {
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

    public float luck() {
        return luck;
    }

    public World world() {
        return world;
    }
}
