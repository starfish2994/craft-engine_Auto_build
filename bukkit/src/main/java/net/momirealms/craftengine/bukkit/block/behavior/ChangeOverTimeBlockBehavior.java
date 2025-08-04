package net.momirealms.craftengine.bukkit.block.behavior;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.bukkit.GameEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.util.Vector;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitBlockInWorld;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.World;
import net.momirealms.sparrow.nbt.CompoundTag;

public class ChangeOverTimeBlockBehavior extends BukkitBlockBehavior{
    public static final Factory FACTORY = new Factory();
    private final float delay;
    private final Key nextBlock;

    public ChangeOverTimeBlockBehavior(CustomBlock customBlock, float delay, Key nextBlock) {
        super(customBlock);
        this.delay = delay;
        this.nextBlock = nextBlock;
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        if (shouldChange(args)) {
            Optional<CustomBlock> optionalNewCustomBlock = BukkitBlockManager.instance().blockById(nextBlock);
            if (optionalNewCustomBlock.isPresent()) {

                Object blockState = args[0];
                World level = (World) args[1];
                BlockPos blockPos = (BlockPos) args[2];
                Optional<ImmutableBlockState> optionalCurrentState = BlockStateUtils.getOptionalCustomBlockState(blockState);
                if (optionalCurrentState.isEmpty()) {
                    return;
                }
                CompoundTag compoundTag = optionalCurrentState.get().propertiesNbt();
                ImmutableBlockState newState = optionalNewCustomBlock.get().getBlockState(compoundTag);
                BukkitBlockInWorld blockInWorld = (BukkitBlockInWorld) level.getBlockAt(LocationUtils.fromBlockPos(blockPos));
                BlockFormEvent event = new BlockFormEvent(blockInWorld.block(), BlockStateUtils.fromBlockData(newState.customBlockState().handle()).createBlockState());
                if(event.callEvent()){

                    FastNMS.INSTANCE.method$LevelWriter$setBlock(level.serverWorld(),  blockPos, newState.customBlockState().handle(), UpdateOption.UPDATE_ALL_IMMEDIATE.flags());
                    blockInWorld.block().getWorld().sendGameEvent(null, GameEvent.BLOCK_CHANGE, new Vector(blockPos.x(), blockPos.y(), blockPos.z()));
                }
            }
        }
    }

    private boolean shouldChange(Object[] args) {
        return RandomUtils.generateRandomFloat(0F, 1F) < this.delay;
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            float delay = Float.valueOf(arguments.getOrDefault("delay", 0.05688889F).toString());
            String nextBlock = arguments.getOrDefault("next-block", "minecraft:air").toString();
            return new ChangeOverTimeBlockBehavior(block, delay, Key.from(nextBlock));
        }
    }
}
