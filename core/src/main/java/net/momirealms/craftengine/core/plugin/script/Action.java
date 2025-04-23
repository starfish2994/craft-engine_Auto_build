package net.momirealms.craftengine.core.plugin.script;

import java.util.concurrent.CompletableFuture;

public interface Action<T> {

    CompletableFuture<T> execute(T t);
}
