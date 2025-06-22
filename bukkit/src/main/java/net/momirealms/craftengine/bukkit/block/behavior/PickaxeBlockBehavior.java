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
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;

import java.util.Map;
import java.util.concurrent.Callable;

public class PickaxeBlockBehavior extends FacingTriggerableBlockBehavior {
    public static final Factory FACTORY = new Factory();

    public PickaxeBlockBehavior(CustomBlock customBlock, Property<Direction> facing, Property<Boolean> triggered) {
        super(customBlock, facing, triggered);
    }

    @Override
    protected Object getTickPriority() {
        return CoreReflections.instance$TickPriority$EXTREMELY_HIGH;
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return;
        Object blockPos = FastNMS.INSTANCE.method$BlockPos$relative(pos, DirectionUtils.toNMSDirection(blockState.get(this.facingProperty)));
        if (!FastNMS.INSTANCE.method$BlockStateBase$isAir(FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, blockPos))) {
            FastNMS.INSTANCE.method$LevelWriter$destroyBlock(level, blockPos, true, null, 512);
        }
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        @SuppressWarnings("unchecked")
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Direction> facing = (Property<Direction>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.pickaxe.missing_facing");
            Property<Boolean> triggered = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("triggered"), "warning.config.block.behavior.pickaxe.missing_triggered");
            return new PickaxeBlockBehavior(block, facing, triggered);
        }
    }
}
