package net.momirealms.craftengine.core.util.context;

import java.util.Optional;

public interface Context {

    ContextHolder contexts();

    <T> Optional<T> getOptionalParameter(ContextKey<T> parameter);

    <T> T getParameterOrThrow(ContextKey<T> parameter);

    <T> Context withParameter(ContextKey<T> parameter, T value);
}
