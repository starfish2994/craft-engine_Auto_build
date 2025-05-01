package net.momirealms.craftengine.bukkit.compatibility.worldedit;

import com.fastasyncworldedit.core.configuration.Settings;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.EmptyBlock;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.world.ChunkPos;
import org.bukkit.Bukkit;

public class FastAsyncWorldEditDelegate extends AbstractDelegateExtent {
    protected FastAsyncWorldEditDelegate(Extent extent) {
        super(extent);
    }

    public static void init() {
        Settings.settings().EXTENT.ALLOWED_PLUGINS.add(FastAsyncWorldEditDelegate.class.getCanonicalName());
        WorldEdit.getInstance().getEventBus().register(new Object() {
            @Subscribe
            @SuppressWarnings("unused")
            public void onEditSessionEvent(EditSessionEvent event) {
                if (event.getStage() != EditSession.Stage.BEFORE_HISTORY) return;
                event.setExtent(new FastAsyncWorldEditDelegate(event.getExtent()));
            }
        });
    }

    @Override
    public int setBlocks(final Region region, final Pattern pattern) {
        this.processBlocks(region, pattern);
        return super.setBlocks(region, pattern);
    }

    @Override
    public int replaceBlocks(Region region, Mask mask, Pattern pattern) {
        this.processBlocks(region, pattern);
        return super.replaceBlocks(region, mask, pattern);
    }

    private void processBlocks(Region region, Pattern pattern) {
        try {
            for (BlockVector3 position : region) {
                BaseBlock blockState = pattern.applyBlock(position);
                BlockState oldBlockState = getBlock(position);
                int blockX = position.x();
                int blockY = position.y();
                int blockZ = position.z();
                int chunkX = blockX >> 4;
                int chunkZ = blockZ >> 4;
                int stateId = BlockStateUtils.blockDataToId(Bukkit.createBlockData(blockState.getAsString()));
                int oldStateId = BlockStateUtils.blockDataToId(Bukkit.createBlockData(oldBlockState.getAsString()));
                if (BlockStateUtils.isVanillaBlock(stateId) && BlockStateUtils.isVanillaBlock(oldStateId)) continue;
                var weWorld = region.getWorld();
                if (weWorld == null) continue;
                var world = Bukkit.getWorld(weWorld.getName());
                if (world == null) continue;
                var ceWorld = CraftEngine.instance().worldManager().getWorld(world.getUID());
                if (ceWorld == null) continue;
                var ceChunk = ceWorld.getChunkAtIfLoaded(chunkX, chunkZ);
                if (ceChunk == null) {
                    ceChunk = ceWorld.worldDataStorage().readChunkAt(ceWorld, new ChunkPos(chunkX, chunkZ));
                }
                var immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
                if (immutableBlockState == null) {
                    ceChunk.setBlockState(blockX, blockY, blockZ, EmptyBlock.STATE);
                } else {
                    ceChunk.setBlockState(blockX, blockY, blockZ, immutableBlockState);
                }
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Error when recording FastAsyncWorldEdit operation blocks", e);
        }
    }
}
