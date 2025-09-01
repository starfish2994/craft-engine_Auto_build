package net.momirealms.craftengine.core.world;

import ca.spottedleaf.concurrentutil.map.ConcurrentLong2ReferenceChainedHashTable;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.block.entity.tick.TickingBlockEntity;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import net.momirealms.craftengine.core.world.chunk.storage.StorageAdaptor;
import net.momirealms.craftengine.core.world.chunk.storage.WorldDataStorage;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CEWorld {
    public static final String REGION_DIRECTORY = "craftengine";
    protected final World world;
    protected final ConcurrentLong2ReferenceChainedHashTable<CEChunk> loadedChunkMap;
    protected final WorldDataStorage worldDataStorage;
    protected final WorldHeight worldHeightAccessor;
    protected final Set<SectionPos> updatedSectionSet = ConcurrentHashMap.newKeySet(128);
    protected final List<TickingBlockEntity> tickingBlockEntities = new ArrayList<>();
    protected final List<TickingBlockEntity> pendingTickingBlockEntities = new ArrayList<>();
    protected boolean isTickingBlockEntities = false;

    private CEChunk lastChunk;
    private long lastChunkPos;

    public CEWorld(World world, StorageAdaptor adaptor) {
        this.world = world;
        this.loadedChunkMap = ConcurrentLong2ReferenceChainedHashTable.createWithCapacity(1024, 0.5f);
        this.worldDataStorage = adaptor.adapt(world);
        this.worldHeightAccessor = world.worldHeight();
        this.lastChunkPos = ChunkPos.INVALID_CHUNK_POS;
    }

    public CEWorld(World world, WorldDataStorage dataStorage) {
        this.world = world;
        this.loadedChunkMap = ConcurrentLong2ReferenceChainedHashTable.createWithCapacity(1024, 0.5f);
        this.worldDataStorage = dataStorage;
        this.worldHeightAccessor = world.worldHeight();
        this.lastChunkPos = ChunkPos.INVALID_CHUNK_POS;
    }

    public String name() {
        return this.world.name();
    }

    public UUID uuid() {
        return this.world.uuid();
    }

    public void save() {
        try {
            for (ConcurrentLong2ReferenceChainedHashTable.TableEntry<CEChunk> entry : this.loadedChunkMap.entrySet()) {
                CEChunk chunk = entry.getValue();
                if (chunk.dirty()) {
                    worldDataStorage.writeChunkAt(new ChunkPos(entry.getKey()), chunk);
                    chunk.setDirty(false);
                }
            }
        } catch (IOException e) {
            CraftEngine.instance().logger().warn("Failed to save world chunks", e);
        }
    }

    public World world() {
        return world;
    }

    public boolean isChunkLoaded(final long chunkPos) {
        return loadedChunkMap.containsKey(chunkPos);
    }

    public void addLoadedChunk(CEChunk chunk) {
        this.loadedChunkMap.put(chunk.chunkPos().longKey(), chunk);
    }

    public void removeLoadedChunk(CEChunk chunk) {
        this.loadedChunkMap.remove(chunk.chunkPos().longKey());
        if (this.lastChunk == chunk) {
            this.lastChunk = null;
            this.lastChunkPos = ChunkPos.INVALID_CHUNK_POS;
        }
    }

    @Nullable
    public CEChunk getChunkAtIfLoaded(long chunkPos) {
        if (chunkPos == this.lastChunkPos) {
            return this.lastChunk;
        }
        CEChunk chunk = this.loadedChunkMap.get(chunkPos);
        if (chunk != null) {
            this.lastChunk = chunk;
            this.lastChunkPos = chunkPos;
        }
        return chunk;
    }

    @Nullable
    public CEChunk getChunkAtIfLoaded(int x, int z) {
        return getChunkAtIfLoaded(ChunkPos.asLong(x, z));
    }

    @Nullable
    public ImmutableBlockState getBlockStateAtIfLoaded(int x, int y, int z) {
        CEChunk chunk = getChunkAtIfLoaded(x >> 4, z >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockState(x, y, z);
    }

    @Nullable
    public ImmutableBlockState getBlockStateAtIfLoaded(BlockPos blockPos) {
        CEChunk chunk = getChunkAtIfLoaded(blockPos.x() >> 4, blockPos.z() >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockState(blockPos);
    }

    public boolean setBlockStateAtIfLoaded(BlockPos blockPos, ImmutableBlockState blockState) {
        if (this.worldHeightAccessor.isOutsideBuildHeight(blockPos)) {
            return false;
        }
        CEChunk chunk = getChunkAtIfLoaded(blockPos.x() >> 4, blockPos.z() >> 4);
        if (chunk == null) {
            return false;
        }
        chunk.setBlockState(blockPos, blockState);
        return true;
    }

    @Nullable
    public BlockEntity getBlockEntityAtIfLoaded(BlockPos blockPos) {
        if (this.worldHeightAccessor.isOutsideBuildHeight(blockPos)) {
            return null;
        }
        CEChunk chunk = getChunkAtIfLoaded(blockPos.x() >> 4, blockPos.z() >> 4);
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockEntity(blockPos);
    }

    public WorldDataStorage worldDataStorage() {
        return worldDataStorage;
    }

    public void sectionLightUpdated(SectionPos pos) {
        this.updatedSectionSet.add(pos);
    }

    public void sectionLightUpdated(Collection<SectionPos> pos) {
        this.updatedSectionSet.addAll(pos);
    }

    public WorldHeight worldHeight() {
        return this.worldHeightAccessor;
    }

    public void tick() {
        this.tickBlockEntities();
    }

    protected void tickBlockEntities() {
        this.isTickingBlockEntities = true;
        if (!this.pendingTickingBlockEntities.isEmpty()) {
            this.tickingBlockEntities.addAll(this.pendingTickingBlockEntities);
            this.pendingTickingBlockEntities.clear();
        }
        ReferenceOpenHashSet<TickingBlockEntity> toRemove = new ReferenceOpenHashSet<>();
        for (TickingBlockEntity blockEntity : this.tickingBlockEntities) {
            if (!blockEntity.isValid()) {
                blockEntity.tick();
            } else {
                toRemove.add(blockEntity);
            }
        }
        this.tickingBlockEntities.removeAll(toRemove);
        this.isTickingBlockEntities = false;
    }
}
