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

import java.util.Map;
import java.util.Optional;
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
        ImmutableBlockState blockState = state.owner().value().defaultState()
                .with(this.facingProperty, context.getHorizontalDirection().toHorizontalDirection())
                .with(this.halfProperty, clickedFace != Direction.DOWN && (clickedFace == Direction.UP || !(context.getClickLocation().y - clickedPos.y() > 0.5)) ? SingleBlockHalf.BOTTOM : SingleBlockHalf.TOP);
        if (super.waterloggedProperty != null) {
            Object fluidState = FastNMS.INSTANCE.method$Level$getFluidState(context.getLevel().serverWorld(), LocationUtils.toBlockPos(clickedPos));
            blockState = blockState.with(this.waterloggedProperty, FastNMS.INSTANCE.method$FluidState$getType(fluidState) == MFluids.WATER);
        }
        return blockState.with(this.shapeProperty, getStairsShape(blockState, context.getLevel().serverWorld(), clickedPos));
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
        if (super.waterloggedProperty != null && immutableBlockState.get(this.waterloggedProperty)) {
            FastNMS.INSTANCE.method$LevelAccessor$scheduleFluidTick(VersionHelper.isOrAbove1_21_2() ? args[2] : args[3], VersionHelper.isOrAbove1_21_2() ? args[3] : args[4], MFluids.WATER, 5);
        }
        Direction direction = DirectionUtils.fromNMSDirection(VersionHelper.isOrAbove1_21_2() ? args[4] : args[1]);
        StairsShape stairsShape = getStairsShape(immutableBlockState, level, LocationUtils.fromBlockPos(blockPos));
        return direction.axis().isHorizontal()
                ? immutableBlockState.with(this.shapeProperty, stairsShape).customBlockState().handle()
                : superMethod.call();
    }

    private StairsShape getStairsShape(ImmutableBlockState state, Object level, BlockPos pos) {
        Direction direction = state.get(this.facingProperty).toDirection();
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(pos.relative(direction)));
        int stateId = BlockStateUtils.blockStateToId(blockState);
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
        if (immutableBlockState != null && !immutableBlockState.isEmpty()) {
            if (isStairs(blockState) && state.get(this.halfProperty) == immutableBlockState.get(this.halfProperty)) {
                Direction direction1 = immutableBlockState.get(this.facingProperty).toDirection();
                if (direction1.axis() != state.get(this.facingProperty).toDirection().axis() && canTakeShape(state, level, pos, direction1.opposite())) {
                    if (direction1 == direction.counterClockWise()) {
                        return StairsShape.OUTER_LEFT;
                    }
                    return StairsShape.OUTER_RIGHT;
                }
            }
        } else if (isStairs(blockState)) {
            // 处理可能是原版楼梯
            // try {
            //     Object nmsHalf = CoreReflections.method$StateHolder$getValue.invoke(blockState, CoreReflections.instance$StairBlock$HALF);
            //     SingleBlockHalf half = SingleBlockHalf.valueOf(nmsHalf.toString().toUpperCase(Locale.ROOT));
            //     if (state.get(this.halfProperty).equals(half)) {
            //         Object nmsFacing = CoreReflections.method$StateHolder$getValue.invoke(blockState, CoreReflections.instance$StairBlock$FACING);
            //         Direction direction1 = DirectionUtils.fromNMSDirection(nmsFacing);
            //         if (direction1.axis() != state.get(this.facingProperty).toDirection().axis() && canTakeShape(state, level, pos, direction1.opposite())) {
            //             if (direction1 == direction.counterClockWise()) {
            //                 return StairsShape.OUTER_LEFT;
            //             }
            //             return StairsShape.OUTER_RIGHT;
            //         }
            //     }
            // } catch (Exception e) {
            //     CraftEngine.instance().logger().warn("Failed to get facing from blockState", e);
            // }
        }

        Object blockState1 = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(pos.relative(direction.opposite())));
        int stateId1 = BlockStateUtils.blockStateToId(blockState1);
        ImmutableBlockState immutableBlockState1 = BukkitBlockManager.instance().getImmutableBlockState(stateId1);
        if (immutableBlockState1 != null && !immutableBlockState1.isEmpty()) {
            if (isStairs(blockState1) && state.get(this.halfProperty) == immutableBlockState1.get(this.halfProperty)) {
                Direction direction2 = immutableBlockState1.get(this.facingProperty).toDirection();
                if (direction2.axis() != state.get(this.facingProperty).toDirection().axis() && canTakeShape(state, level, pos, direction2)) {
                    if (direction2 == direction.counterClockWise()) {
                        return StairsShape.INNER_LEFT;
                    }
                    return StairsShape.INNER_RIGHT;
                }
            }
        } else if (isStairs(blockState1)) {
            // 处理可能是原版楼梯
            // try {
            //     Object nmsHalf = CoreReflections.method$StateHolder$getValue.invoke(blockState1, CoreReflections.instance$StairBlock$HALF);
            //     SingleBlockHalf half = SingleBlockHalf.valueOf(nmsHalf.toString().toUpperCase(Locale.ROOT));
            //     if (state.get(this.halfProperty).equals(half)) {
            //         Object nmsFacing = CoreReflections.method$StateHolder$getValue.invoke(blockState1, CoreReflections.instance$StairBlock$FACING);
            //         Direction direction1 = DirectionUtils.fromNMSDirection(nmsFacing);
            //         if (direction1.axis() != state.get(this.facingProperty).toDirection().axis() && canTakeShape(state, level, pos, direction1)) {
            //             if (direction1 == direction.counterClockWise()) {
            //                 return StairsShape.INNER_LEFT;
            //             }
            //             return StairsShape.INNER_RIGHT;
            //         }
            //     }
            // } catch (Exception e) {
            //     CraftEngine.instance().logger().warn("Failed to get facing from blockState", e);
            // }
        }

        return StairsShape.STRAIGHT;
    }

    private boolean isStairs(Object state) {
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (immutableBlockState == null || immutableBlockState.isEmpty()) {
            return FastNMS.INSTANCE.method$BlockState$getBlock(state).equals(CoreReflections.clazz$StairBlock);
        }
        Optional<StairsBlockBehavior> optionalBehavior = immutableBlockState.behavior().getAs(StairsBlockBehavior.class);
        return optionalBehavior.isPresent();
    }

    private boolean canTakeShape(ImmutableBlockState state, Object level, BlockPos pos, Direction face) {
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(pos.relative(face)));
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (immutableBlockState == null || immutableBlockState.isEmpty()) {
            // 处理可能是原版楼梯
            // try {
            //     Object nmsFacing = CoreReflections.method$StateHolder$getValue.invoke(blockState, CoreReflections.instance$StairBlock$FACING);
            //     Direction direction = DirectionUtils.fromNMSDirection(nmsFacing);
            //     if (direction != state.get(this.facingProperty).toDirection()) return true;
            //     Object nmsHalf = CoreReflections.method$StateHolder$getValue.invoke(blockState, CoreReflections.instance$StairBlock$HALF);
            //     SingleBlockHalf half = SingleBlockHalf.valueOf(nmsHalf.toString().toUpperCase(Locale.ROOT));
            //     if (half != state.get(this.halfProperty)) return true;
            // } catch (Exception e) {
            //     CraftEngine.instance().logger().warn("Failed to handle canTakeShape", e);
            // }
            return !isStairs(blockState);
        }
        return !isStairs(blockState) || immutableBlockState.get(this.facingProperty) != state.get(this.facingProperty) || immutableBlockState.get(this.halfProperty) != state.get(this.halfProperty);
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
