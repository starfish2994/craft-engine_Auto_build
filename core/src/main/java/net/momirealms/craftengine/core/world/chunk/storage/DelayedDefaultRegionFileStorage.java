package net.momirealms.craftengine.core.world.chunk.storage;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class DelayedDefaultRegionFileStorage extends DefaultRegionFileStorage {
    private final Cache<ChunkPos, CEChunk> chunkCache;

    public DelayedDefaultRegionFileStorage(Path directory, int time) {
        super(directory);
        this.chunkCache = Caffeine.newBuilder()
                .expireAfterWrite(time, TimeUnit.SECONDS)
                .removalListener((ChunkPos key, CEChunk value, RemovalCause cause) -> {
                    if (key == null || value == null) {
                        return;
                    }
                    if (cause == RemovalCause.EXPIRED || cause == RemovalCause.SIZE) {
                        try {
                            super.writeChunkAt(key, value);
                        } catch (IOException e) {
                            CraftEngine.instance().logger().warn("Failed to write chunk at " + key, e);
                        }
                    }
                })
                .build();
    }

    @Override
    public @NotNull CEChunk readChunkAt(@NotNull CEWorld world, @NotNull ChunkPos pos) throws IOException {
        CEChunk chunk = this.chunkCache.asMap().remove(pos);
        if (chunk != null) {
            return chunk;
        }
        return super.readChunkAt(world, pos);
    }

    @Override
    public void writeChunkAt(@NotNull ChunkPos pos, @NotNull CEChunk chunk) throws IOException {
        if (chunk.isEmpty()) {
            super.writeChunkTagAt(pos, null);
            return;
        }
        this.chunkCache.put(pos, chunk);
    }

    @Override
    public synchronized void close() throws IOException {
        this.saveCache();
        super.close();
    }

    private void saveCache() {
        try {
            for (var chunk : this.chunkCache.asMap().entrySet()) {
                super.writeChunkAt(chunk.getKey(), chunk.getValue());
            }
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("Failed to save chunks", e);
        }
        this.chunkCache.invalidateAll();
    }
}