package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;

public class SturdyBaseBlockBehavior extends AbstractCanSurviveBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Direction direction;
    private final boolean stackable;

    public SturdyBaseBlockBehavior(CustomBlock block, int delay, Direction direction, boolean stackable) {
        super(block, delay);
        this.direction = direction;
        this.stackable = stackable;
    }

    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws Exception {
        int x = FastNMS.INSTANCE.field$Vec3i$x(blockPos) + this.direction.stepX();
        int y = FastNMS.INSTANCE.field$Vec3i$y(blockPos) + this.direction.stepY();
        int z = FastNMS.INSTANCE.field$Vec3i$z(blockPos) + this.direction.stepZ();
        Object targetPos = FastNMS.INSTANCE.constructor$BlockPos(x, y, z);
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(world, targetPos);
        if ((boolean) CoreReflections.method$BlockStateBase$isFaceSturdy.invoke(
                blockState, world, targetPos, DirectionUtils.toNMSDirection(this.direction.opposite()),
                CoreReflections.instance$SupportType$FULL
        )) {
            return true;
        }
        if (!this.stackable) {
            return false;
        }
        ImmutableBlockState targetCustomState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (targetCustomState == null || targetCustomState.isEmpty()) return false;
        return targetCustomState.owner().value() == super.customBlock;
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            int delay = ResourceConfigUtils.getAsInt(arguments.getOrDefault("delay", 0), "delay");
            Direction direction = Direction.valueOf(arguments.getOrDefault("direction", "down").toString().toUpperCase(Locale.ENGLISH));
            boolean stackable = (boolean) arguments.getOrDefault("stackable", false);
            return new SturdyBaseBlockBehavior(block, delay, direction, stackable);
        }
    }
}
