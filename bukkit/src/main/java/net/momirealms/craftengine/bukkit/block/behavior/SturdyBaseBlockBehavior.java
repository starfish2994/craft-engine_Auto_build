package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Locale;
import java.util.Map;

public class SturdyBaseBlockBehavior extends AbstractCanSurviveBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Direction direction;

    public SturdyBaseBlockBehavior(CustomBlock block, int delay, Direction direction) {
        super(block, delay);
        this.direction = direction;
    }

    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws Exception {
        int x = FastNMS.INSTANCE.field$Vec3i$x(blockPos) + this.direction.stepX();
        int y = FastNMS.INSTANCE.field$Vec3i$y(blockPos) + this.direction.stepY();
        int z = FastNMS.INSTANCE.field$Vec3i$z(blockPos) + this.direction.stepZ();
        Object targetPos = FastNMS.INSTANCE.constructor$BlockPos(x, y, z);
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(world, targetPos);
        return (boolean) CoreReflections.method$BlockStateBase$isFaceSturdy.invoke(
                blockState, world, blockPos, DirectionUtils.toNMSDirection(this.direction.opposite()),
                CoreReflections.instance$SupportType$FULL
        );
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            int delay = ResourceConfigUtils.getAsInt(arguments.getOrDefault("delay", 0), "delay");
            Direction direction = Direction.valueOf(arguments.getOrDefault("direction", "down").toString().toUpperCase(Locale.ROOT));
            return new SturdyBaseBlockBehavior(block, delay, direction);
        }
    }
}
