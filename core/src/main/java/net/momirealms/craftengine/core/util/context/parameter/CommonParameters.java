package net.momirealms.craftengine.core.util.context.parameter;

import net.momirealms.craftengine.core.util.context.ContextKey;

public final class CommonParameters {
    private CommonParameters() {}

    public static final ContextKey<Double> RANDOM = ContextKey.of("random");
    public static final ContextKey<Double> LAST_RANDOM = ContextKey.of("last_random");
}
