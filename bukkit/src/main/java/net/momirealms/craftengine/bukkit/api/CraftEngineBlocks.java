package net.momirealms.craftengine.bukkit.api;

import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import org.bukkit.Location;

@SuppressWarnings("DuplicatedCode")
public final class CraftEngineBlocks {

    public static boolean place(Location location, ImmutableBlockState block, UpdateOption option) {
        boolean success;
        try {
            Object worldServer = Reflections.field$CraftWorld$ServerLevel.get(location.getWorld());
            Object blockPos = Reflections.constructor$BlockPos.newInstance(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            Object blockState = block.customBlockState().handle();
            Object oldBlockState = Reflections.method$BlockGetter$getBlockState.invoke(worldServer, blockPos);
            success = (boolean) Reflections.method$LevelWriter$setBlock.invoke(worldServer, blockPos, blockState, option.flags());
            if (success) {
                Reflections.method$BlockStateBase$onPlace.invoke(blockState, worldServer, blockPos, oldBlockState, true);
            }
        } catch (ReflectiveOperationException e) {
            CraftEngine.instance().logger().warn("Failed to set nms block", e);
            return false;
        }
        return success;
    }
}
