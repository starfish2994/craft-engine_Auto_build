package net.momirealms.craftengine.core.util.context;

import java.util.Optional;

public abstract class CommonContext implements Context {
    protected final ContextHolder contexts;

    public CommonContext(ContextHolder contexts) {
        this.contexts = contexts;
    }

    @Override
    public ContextHolder contexts() {
        return contexts;
    }

    @Override
    public <T> Optional<T> getOptionalParameter(ContextKey<T> parameter) {
        return this.contexts.getOptional(parameter);
    }

    @Override
    public <T> T getParameterOrThrow(ContextKey<T> parameter) {
        return this.contexts.getOrThrow(parameter);
    }

    @Override
    public <T> CommonContext withParameter(ContextKey<T> parameter, T value) {
        this.contexts.withParameter(parameter, value);
        return this;
    }
}
