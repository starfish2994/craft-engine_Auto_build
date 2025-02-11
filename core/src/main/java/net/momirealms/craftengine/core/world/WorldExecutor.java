package net.momirealms.craftengine.core.world;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorldExecutor implements Executor {

    private final ExecutorService threadPool;

    public WorldExecutor(CEWorld world) {
        this.threadPool = Executors.newSingleThreadExecutor(r -> new WorldThread(r, "CEWorld-Thread-" + world.world().name(), world));
    }

    @Override
    public void execute(@NotNull Runnable command) {
        this.threadPool.execute(command);
    }
}
