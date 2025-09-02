package net.momirealms.craftengine.core.world.chunk;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.momirealms.craftengine.core.block.EmptyBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.entity.BlockEntity;
import net.momirealms.craftengine.core.plugin.logger.Debugger;
import net.momirealms.craftengine.core.world.*;
import net.momirealms.craftengine.core.world.chunk.serialization.DefaultBlockEntitySerializer;
import net.momirealms.sparrow.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CEChunk {
    private boolean loaded;
    private final CEWorld world;
    private final ChunkPos chunkPos;
    private final CESection[] sections;
    private final WorldHeight worldHeightAccessor;
    private final Map<BlockPos, BlockEntity> blockEntities;
    private boolean dirty;

    public CEChunk(CEWorld world, ChunkPos chunkPos) {
        this.world = world;
        this.chunkPos = chunkPos;
        this.worldHeightAccessor = world.worldHeight();
        this.sections = new CESection[this.worldHeightAccessor.getSectionsCount()];
        this.blockEntities = new Object2ObjectOpenHashMap<>(16, 0.5f);
        this.fillEmptySection();
    }

    public CEChunk(CEWorld world, ChunkPos chunkPos, CESection[] sections, ListTag blockEntitiesTag) {
        this.world = world;
        this.chunkPos = chunkPos;
        this.blockEntities = new Object2ObjectOpenHashMap<>(Math.max(blockEntitiesTag.size(), 16), 0.5f);
        this.worldHeightAccessor = world.worldHeight();
        int sectionCount = this.worldHeightAccessor.getSectionsCount();
        this.sections = new CESection[sectionCount];
        if (sections != null) {
            for (CESection section : sections) {
                if (section != null) {
                    int index = sectionIndex(section.sectionY());
                    this.sections[index] = section;
                }
            }
        }
        this.fillEmptySection();
        List<BlockEntity> blockEntities = DefaultBlockEntitySerializer.deserialize(this, blockEntitiesTag);
        for (BlockEntity blockEntity : blockEntities) {
            this.setBlockEntity(blockEntity);
        }
    }

    public void addBlockEntity(BlockEntity blockEntity) {
        this.setBlockEntity(blockEntity);
    }

    public void removeBlockEntity(BlockPos blockPos) {
        BlockEntity removedBlockEntity = this.blockEntities.remove(blockPos);
        if (removedBlockEntity != null) {
            removedBlockEntity.setValid(false);
        }
    }

    public void setBlockEntity(BlockEntity blockEntity) {
        BlockPos pos = blockEntity.pos();
        ImmutableBlockState blockState = this.getBlockState(pos);
        if (!blockState.hasBlockEntity()) {
            Debugger.BLOCK_ENTITY.debug(() -> "Failed to add invalid block entity " + blockEntity.saveAsTag() + " at " + pos);
            return;
        }
        // 设置方块实体所在世界
        blockEntity.setWorld(this.world);
        blockEntity.setValid(true);
        BlockEntity previous = this.blockEntities.put(pos, blockEntity);
        // 标记旧的方块实体无效
        if (previous != null && previous != blockEntity) {
            previous.setValid(false);
        }
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos pos) {
        BlockEntity blockEntity = this.blockEntities.get(pos);
        if (blockEntity == null) {
            blockEntity = createBlockEntity(pos);
            if (blockEntity != null) {
                this.addBlockEntity(blockEntity);
            }
        } else {
            if (!blockEntity.isValid()) {
                this.blockEntities.remove(pos);
                return null;
            }
        }
        return blockEntity;
    }

    private BlockEntity createBlockEntity(BlockPos pos) {
        ImmutableBlockState blockState = this.getBlockState(pos);
        if (!blockState.hasBlockEntity()) {
            return null;
        }
        return blockState.blockEntityType().factory().create(pos, blockState);
    }

    public Map<BlockPos, BlockEntity> blockEntities() {
        return Collections.unmodifiableMap(this.blockEntities);
    }

    public boolean dirty() {
        return this.dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isEmpty() {
        if (!this.blockEntities.isEmpty()) return false;
        for (CESection section : this.sections) {
            if (section != null && !section.statesContainer().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void fillEmptySection() {
        for (int i = 0; i < sections.length; ++i) {
            if (sections[i] == null) {
                sections[i] = new CESection(world.worldHeight().getSectionYFromSectionIndex(i),
                        new PalettedContainer<>(null, EmptyBlock.STATE, PalettedContainer.PaletteProvider.CUSTOM_BLOCK_STATE));
            }
        }
    }

    public void setBlockState(BlockPos pos, ImmutableBlockState state) {
        this.setBlockState(pos.x(), pos.y(), pos.z(), state);
    }

    public void setBlockState(int x, int y, int z, ImmutableBlockState state) {
        int index = sectionIndex(SectionPos.blockToSectionCoord(y));
        CESection section = this.sections[index];
        if (section == null) {
            return;
        }
        ImmutableBlockState previous = section.setBlockState((y & 15) << 8 | (z & 15) << 4 | x & 15, state);
        if (previous != state) {
            setDirty(true);
        }
    }

    @NotNull
    public ImmutableBlockState getBlockState(BlockPos pos) {
        return getBlockState(pos.x(), pos.y(), pos.z());
    }

    @NotNull
    public ImmutableBlockState getBlockState(int x, int y, int z) {
        int index = sectionIndex(SectionPos.blockToSectionCoord(y));
        CESection section = this.sections[index];
        if (section == null) {
            return EmptyBlock.STATE;
        }
        return section.getBlockState((y & 15) << 8 | (z & 15) << 4 | x & 15);
    }

    @Nullable
    public CESection sectionByIndex(int index) {
        return this.sections[index];
    }

    @NotNull
    public CESection sectionById(int sectionId) {
        return this.sections[sectionIndex(sectionId)];
    }

    public int sectionIndex(int sectionId) {
        return sectionId - this.worldHeightAccessor.getMinSection();
    }

    public int sectionY(int sectionIndex) {
        return sectionIndex + this.worldHeightAccessor.getMinSection();
    }

    @NotNull
    public CEWorld world() {
        return world;
    }

    @NotNull
    public ChunkPos chunkPos() {
        return chunkPos;
    }

    @NotNull
    public CESection[] sections() {
        return sections;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load() {
        if (this.loaded) return;
        this.world.addLoadedChunk(this);
        this.loaded = true;
    }

    public void unload() {
        if (!this.loaded) return;
        this.world.removeLoadedChunk(this);
        this.loaded = false;
    }
}
