package net.momirealms.craftengine.core.plugin.context.number;

import net.momirealms.craftengine.core.plugin.context.Context;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.RandomUtils;

import java.util.Map;

public class UniformNumberProvider implements NumberProvider {
    public static final Factory FACTORY = new Factory();
    private final NumberProvider min;
    private final NumberProvider max;

    public UniformNumberProvider(NumberProvider min, NumberProvider max) {
        this.min = min;
        this.max = max;
    }

    public UniformNumberProvider(float min, float max) {
        this.min = new FixedNumberProvider(min);
        this.max = new FixedNumberProvider(max);
    }

    @Override
    public int getInt(Context context) {
        return RandomUtils.generateRandomInt(this.min.getInt(context), this.max.getInt(context));
    }

    @Override
    public float getFloat(Context context) {
        return RandomUtils.generateRandomFloat(this.min.getFloat(context), this.max.getFloat(context));
    }

    @Override
    public Key type() {
        return NumberProviders.UNIFORM;
    }

    public static class Factory implements NumberProviderFactory {
        @Override
        public NumberProvider create(Map<String, Object> arguments) {
            Object min = arguments.getOrDefault("min", 1);
            Object max = arguments.getOrDefault("max", 1);
            return new UniformNumberProvider(NumberProviders.fromObject(min), NumberProviders.fromObject(max));
        }
    }
}
