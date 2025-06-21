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
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SturdyBaseBlockBehavior extends AbstractCanSurviveBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Direction direction;
    private final boolean stackable;
    private final List<Object> supportTypes;

    public SturdyBaseBlockBehavior(CustomBlock block, int delay, Direction direction, boolean stackable, List<Object> supportTypes) {
        super(block, delay);
        this.direction = direction;
        this.stackable = stackable;
        this.supportTypes = supportTypes;
    }

    @Override
    protected boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws Exception {
        int x = FastNMS.INSTANCE.field$Vec3i$x(blockPos) + this.direction.stepX();
        int y = FastNMS.INSTANCE.field$Vec3i$y(blockPos) + this.direction.stepY();
        int z = FastNMS.INSTANCE.field$Vec3i$z(blockPos) + this.direction.stepZ();
        Object targetPos = FastNMS.INSTANCE.constructor$BlockPos(x, y, z);
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(world, targetPos);
        for (Object supportType : this.supportTypes) {
            if ((boolean) CoreReflections.method$BlockStateBase$isFaceSturdy.invoke(
                    blockState, world, targetPos, DirectionUtils.toNMSDirection(this.direction.opposite()),
                    supportType
            )) {
                return true;
            }
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
            List<Object> supportTypes = MiscUtils.getAsStringList(arguments.getOrDefault("support-types", List.of("full")))
                    .stream()
                    .map(it -> {
                        if (it.equalsIgnoreCase("full")) {
                            return CoreReflections.instance$SupportType$FULL;
                        } else if (it.equalsIgnoreCase("rigid")) {
                            return CoreReflections.instance$SupportType$RIGID;
                        } else if (it.equalsIgnoreCase("center")) {
                            return CoreReflections.instance$SupportType$CENTER;
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
            return new SturdyBaseBlockBehavior(block, delay, direction, stackable, supportTypes);
        }
    }
}
