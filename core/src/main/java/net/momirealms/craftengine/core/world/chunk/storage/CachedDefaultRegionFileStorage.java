package net.momirealms.craftengine.core.world.chunk.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Scheduler;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class CachedDefaultRegionFileStorage extends DefaultRegionFileStorage {
    private final Cache<ChunkPos, CEChunk> chunkCache;

    public CachedDefaultRegionFileStorage(Path directory) {
        super(directory);
        this.chunkCache = Caffeine.newBuilder()
                .executor(CraftEngine.instance().scheduler().async())
                .scheduler(Scheduler.systemScheduler())
                .expireAfterAccess(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public @NotNull CEChunk readChunkAt(@NotNull CEWorld world, @NotNull ChunkPos pos) throws IOException {
        CEChunk chunk = this.chunkCache.getIfPresent(pos);
        if (chunk != null) {
            return chunk;
        }
        chunk = super.readChunkAt(world, pos);
        this.chunkCache.put(pos, chunk);
        return chunk;
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
    }
}