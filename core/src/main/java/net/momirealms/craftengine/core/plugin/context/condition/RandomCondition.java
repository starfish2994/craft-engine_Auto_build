package net.momirealms.craftengine.core.plugin.context.condition;

import net.momirealms.craftengine.core.plugin.context.Condition;
import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Factory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;

public class RandomCondition<CTX extends Context> implements Condition<CTX> {
    private final float chance;

    public RandomCondition(float chance) {
        this.chance = chance;
    }

    @Override
    public Key type() {
        return SharedConditions.RANDOM;
    }

    @Override
    public boolean test(CTX ctx) {
        return RandomUtils.generateRandomFloat(0, 1) < this.chance;
    }

    public static class FactoryImpl<CTX extends Context> implements Factory<Condition<CTX>> {

        @Override
        public Condition<CTX> create(Map<String, Object> arguments) {
            float provider = ResourceConfigUtils.getAsFloat(arguments.getOrDefault("value", 0.5f), "value");
            return new RandomCondition<>(provider);
        }
    }
}
