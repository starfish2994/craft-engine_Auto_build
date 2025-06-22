package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Direction;

import java.util.concurrent.Callable;

public abstract class FacingTriggerableBlockBehavior extends BukkitBlockBehavior {
    protected final Property<Direction> facingProperty;
    protected final Property<Boolean> triggeredProperty;

    public FacingTriggerableBlockBehavior(CustomBlock customBlock, Property<Direction> facing, Property<Boolean> triggered) {
        super(customBlock);
        this.facingProperty = facing;
        this.triggeredProperty = triggered;
    }

    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        CraftEngine.instance().logger().warn("FacingTriggerableBlockBehavior.neighborChanged");
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        boolean hasNeighborSignal = FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, pos);
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return;
        boolean triggeredValue = blockState.get(this.triggeredProperty);
        if (hasNeighborSignal && !triggeredValue) {
            FastNMS.INSTANCE.method$LevelAccessor$scheduleBlockTick(level, pos, thisBlock, 1, this.getTickPriority());
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, blockState.with(this.triggeredProperty, true).customBlockState().handle(), 2);
        } else if (!hasNeighborSignal && triggeredValue) {
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, blockState.with(this.triggeredProperty, false).customBlockState().handle(), 2);
        }
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        return state.owner().value().defaultState().with(this.facingProperty, context.getNearestLookingDirection().opposite());
    }

    protected abstract Object getTickPriority();
}
