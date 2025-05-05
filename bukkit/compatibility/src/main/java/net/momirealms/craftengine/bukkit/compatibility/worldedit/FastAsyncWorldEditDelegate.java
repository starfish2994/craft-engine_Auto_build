package net.momirealms.craftengine.bukkit.compatibility.worldedit;

import com.fastasyncworldedit.core.configuration.Settings;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.EmptyBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.CEWorld;
import net.momirealms.craftengine.core.world.ChunkPos;
import net.momirealms.craftengine.core.world.chunk.CEChunk;
import org.bukkit.Bukkit;
import org.bukkit.block.data.BlockData;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class FastAsyncWorldEditDelegate extends AbstractDelegateExtent {
    private final Set<CEChunk> chunksToSave;
    private final CEWorld ceWorld;

    protected FastAsyncWorldEditDelegate(EditSessionEvent event) {
        super(event.getExtent());
        this.chunksToSave = new HashSet<>();
        World weWorld = event.getWorld();
        org.bukkit.World world = Bukkit.getWorld(requireNonNull(weWorld).getName());
        CEWorld ceWorld = CraftEngine.instance().worldManager().getWorld(requireNonNull(world).getUID());
        this.ceWorld = requireNonNull(ceWorld);
    }

    public static void init() {
        Settings.settings().EXTENT.ALLOWED_PLUGINS.add(FastAsyncWorldEditDelegate.class.getCanonicalName());
        WorldEdit.getInstance().getEventBus().register(new Object() {
            @Subscribe
            @SuppressWarnings("unused")
            public void onEditSessionEvent(EditSessionEvent event) {
                if (event.getStage() != EditSession.Stage.BEFORE_HISTORY) return;
                event.setExtent(new FastAsyncWorldEditDelegate(event));
            }
        });
    }

    @Override
    public int setBlocks(final Set<BlockVector3> vset, final Pattern pattern) {
        this.processBlocks(vset, pattern);
        return super.setBlocks(vset, pattern);
    }

    @Override
    public int setBlocks(final Region region, final Pattern pattern) {
        this.processBlocks(region, pattern);
        return super.setBlocks(region, pattern);
    }

    @Override
    public <B extends BlockStateHolder<B>> int setBlocks(final Region region, final B block) {
        this.processBlocks(region, block);
        return super.setBlocks(region, block);
    }

    @Override
    public int replaceBlocks(Region region, Mask mask, Pattern pattern) {
        this.processBlocks(region, pattern);
        return super.replaceBlocks(region, mask, pattern);
    }

    @Override
    public <B extends BlockStateHolder<B>> int replaceBlocks(final Region region, final Set<BaseBlock> filter, final B replacement) {
        this.processBlocks(region, replacement);
        return super.replaceBlocks(region, filter, replacement);
    }

    @Override
    public int replaceBlocks(final Region region, final Set<BaseBlock> filter, final Pattern pattern) {
        this.processBlocks(region, pattern);
        return super.replaceBlocks(region, filter, pattern);
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) {
        try {
            BaseBlock oldBlockState = getBlock(x, y, z).toBaseBlock();
            this.processBlock(x, y, z, block.toBaseBlock(), oldBlockState);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Error when recording FastAsyncWorldEdit operation blocks", e);
        }
        return super.setBlock(x, y, z, block);
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) {
        try {
            BaseBlock oldBlockState = getBlock(position).toBaseBlock();
            this.processBlock(position.x(), position.y(), position.z(), block.toBaseBlock(), oldBlockState);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Error when recording FastAsyncWorldEdit operation blocks", e);
        }
        return super.setBlock(position, block);
    }

    @Override
    protected Operation commitBefore() {
        saveAllChunks();
        return super.commitBefore();
    }

    private void processBlocks(Iterable<BlockVector3> region, Pattern pattern) {
        try {
            for (BlockVector3 position : region) {
                BaseBlock blockState = pattern.applyBlock(position);
                BaseBlock oldBlockState = getBlock(position).toBaseBlock();
                int blockX = position.x();
                int blockY = position.y();
                int blockZ = position.z();
                this.processBlock(blockX, blockY, blockZ, blockState, oldBlockState);
            }
            saveAllChunks();
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Error when recording FastAsyncWorldEdit operation blocks", e);
        }
    }

    private void processBlock(int blockX, int blockY, int blockZ, BaseBlock blockState, BaseBlock oldBlockState) throws IOException {
        int chunkX = blockX >> 4;
        int chunkZ = blockZ >> 4;
        BlockData blockData;
        try {
            blockData = Bukkit.createBlockData(blockState.getAsString());
        } catch (IllegalArgumentException e) {
            blockData = Bukkit.createBlockData(blockState.getBlockType().id());
        }
        int newStateId = BlockStateUtils.blockDataToId(blockData);
//        int oldStateId = BlockStateUtils.blockDataToId(Bukkit.createBlockData(oldBlockState.getAsString()));
        if (BlockStateUtils.isVanillaBlock(newStateId) /* && BlockStateUtils.isVanillaBlock(oldStateId) */)
            return;
        CEChunk ceChunk = Optional.ofNullable(this.ceWorld.getChunkAtIfLoaded(chunkX, chunkZ))
                .orElse(this.ceWorld.worldDataStorage().readChunkAt(this.ceWorld, new ChunkPos(chunkX, chunkZ)));
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(newStateId);
        if (immutableBlockState == null) {
            ceChunk.setBlockState(blockX, blockY, blockZ, EmptyBlock.STATE);
        } else {
            ceChunk.setBlockState(blockX, blockY, blockZ, immutableBlockState);
        }
        this.chunksToSave.add(ceChunk);
    }

    private void saveAllChunks() {
        try {
            for (CEChunk ceChunk : this.chunksToSave) {
                this.ceWorld.worldDataStorage().writeChunkAt(ceChunk.chunkPos(), ceChunk, true);
            }
            this.chunksToSave.clear();
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Error when recording FastAsyncWorldEdit operation chunks", e);
        }
    }
}
