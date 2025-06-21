package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MFluids;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.block.state.properties.SingleBlockHalf;
import net.momirealms.craftengine.core.block.state.properties.StairsShape;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;

public class StairsBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<HorizontalDirection> facingProperty;
    private final Property<SingleBlockHalf> halfProperty;
    private final Property<StairsShape> shapeProperty;

    public StairsBlockBehavior(CustomBlock block, Property<HorizontalDirection> facing, Property<SingleBlockHalf> half, Property<StairsShape> shape) {
        super(block);
        this.facingProperty = facing;
        this.halfProperty = half;
        this.shapeProperty = shape;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        Direction clickedFace = context.getClickedFace();
        BlockPos clickedPos = context.getClickedPos();
        Object fluidState = FastNMS.INSTANCE.method$Level$getFluidState(context.getLevel().serverWorld(), LocationUtils.toBlockPos(clickedPos));
        return state.owner().value().defaultState()
                .with(this.facingProperty, context.getHorizontalDirection().toHorizontalDirection())
                .with(this.halfProperty, clickedFace != Direction.DOWN && (clickedFace == Direction.UP || !(context.getClickLocation().y - clickedPos.y() > 0.5)) ? SingleBlockHalf.BOTTOM : SingleBlockHalf.TOP)
                .with(this.waterloggedProperty, FastNMS.INSTANCE.method$FluidState$getType(fluidState) == MFluids.WATER);
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object level;
        Object blockPos;
        Object blockState = args[0];
        if (VersionHelper.isOrAbove1_21_2()) {
            level = args[1];
            blockPos = args[3];
        } else {
            level = args[3];
            blockPos = args[4];
        }
        int stateId = BlockStateUtils.blockStateToId(blockState);
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
        if (immutableBlockState == null || immutableBlockState.isEmpty()) return blockState;
        Direction direction = DirectionUtils.fromNMSDirection(VersionHelper.isOrAbove1_21_2() ? args[4] : args[0]);
        return direction.axis().isHorizontal()
                ? immutableBlockState.with(this.shapeProperty, getStairsShape(immutableBlockState, level, LocationUtils.fromBlockPos(blockPos))).customBlockState().handle()
                : superMethod.call();
    }

    private StairsShape getStairsShape(ImmutableBlockState state, Object level, BlockPos blockPos) {
        if (!state.customBlockState().isVanillaBlock()) {
            Direction direction = state.get(this.facingProperty).toDirection();
            Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(blockPos.relative(direction)));
            StairsShape straight1 = getStairsShape(state, level, blockPos, blockState, direction);
            if (straight1 != null) return straight1;
            Object blockState1 = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(blockPos.relative(direction.opposite())));
            StairsShape straight = getStairsShape(state, level, blockPos, blockState1, direction);
            if (straight != null) return straight;
        }
        return StairsShape.STRAIGHT;
    }

    @Nullable
    private StairsShape getStairsShape(ImmutableBlockState state, Object level, BlockPos blockPos, Object blockState1, Direction direction) {
        if (isStairs(blockState1)) {
            int stateId = BlockStateUtils.blockStateToId(blockState1);
            ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
            if (immutableBlockState == null || immutableBlockState.isEmpty()) return StairsShape.STRAIGHT;
            if (state.get(this.facingProperty) == immutableBlockState.get(this.facingProperty)) {
                Direction direction1 = immutableBlockState.get(this.facingProperty).toDirection();
                if (direction1.axis() != state.get(this.facingProperty).toDirection().axis() && canTakeShape(state, level, blockPos, direction1.opposite())) {
                    if (direction1 == direction.counterClockWise()) {
                        return StairsShape.OUTER_LEFT;
                    }
                    return StairsShape.OUTER_RIGHT;
                }
            }
        }
        return null;
    }

    private boolean isStairs(Object state) {
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (immutableBlockState == null || immutableBlockState.isEmpty()) {
            return FastNMS.INSTANCE.method$BlockState$getBlock(state).equals(CoreReflections.clazz$StairBlock);
        }
        return immutableBlockState.behavior().equals(this);
    }

    private boolean canTakeShape(ImmutableBlockState state, Object level, BlockPos blockPos, Direction direction) {
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(blockPos.relative(direction)));
        if (!isStairs(blockState)) {
            return false;
        }
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (immutableBlockState == null || immutableBlockState.isEmpty()) {
            return false;
        }
        return immutableBlockState.get(this.facingProperty) != state.get(this.facingProperty) || immutableBlockState.get(this.halfProperty) != state.get(this.halfProperty);
    }

    public static class Factory implements BlockBehaviorFactory {

        @Override
        @SuppressWarnings("unchecked")
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<HorizontalDirection> facing = (Property<HorizontalDirection>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.stairs.missing_facing");
            Property<SingleBlockHalf> half = (Property<SingleBlockHalf>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("half"), "warning.config.block.behavior.stairs.missing_half");
            Property<StairsShape> shape = (Property<StairsShape>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("shape"), "warning.config.block.behavior.stairs.missing_shape");
            return new StairsBlockBehavior(block, facing, half, shape);
        }
    }
}
