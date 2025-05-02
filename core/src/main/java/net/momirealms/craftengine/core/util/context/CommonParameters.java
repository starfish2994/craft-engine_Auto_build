package net.momirealms.craftengine.core.util.context;

public final class CommonParameters {

    private CommonParameters() {}

    public static ContextKey<Double> RANDOM = ContextKey.of("random");
    public static ContextKey<Double> LAST_RANDOM = ContextKey.of("last_random");
}
