package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MFluids;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.block.state.properties.SingleBlockHalf;
import net.momirealms.craftengine.core.block.state.properties.StairsShape;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.world.BlockPos;
import org.jetbrains.annotations.Nullable;

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
        Object fluidState = FastNMS.INSTANCE.method$Level$getFluidState(context.getLevel().serverWorld(), LocationUtils.toBlockPos(clickedPos));
        ImmutableBlockState blockState = state.owner().value().defaultState()
                .with(this.facingProperty, context.getHorizontalDirection().toHorizontalDirection())
                .with(this.halfProperty, clickedFace != Direction.DOWN && (clickedFace == Direction.UP || !(context.getClickLocation().y - clickedPos.y() > 0.5)) ? SingleBlockHalf.BOTTOM : SingleBlockHalf.TOP)
                .with(this.waterloggedProperty, FastNMS.INSTANCE.method$FluidState$getType(fluidState) == MFluids.WATER);
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
        Direction direction = DirectionUtils.fromNMSDirection(VersionHelper.isOrAbove1_21_2() ? args[4] : args[0]);
        StairsShape stairsShape = getStairsShape(immutableBlockState, level, LocationUtils.fromBlockPos(blockPos));
        return direction.axis().isHorizontal()
                ? immutableBlockState.with(this.shapeProperty, stairsShape).customBlockState().handle()
                : superMethod.call();
    }

    @Override
    public Object rotate(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Rotation rot = RotationUtils.fromNMSRotation(args[1]);
        ImmutableBlockState blockState = rotate(state, rot);
        return blockState != null ? blockState.customBlockState().handle() : superMethod.call();
    }

    @Nullable
    private ImmutableBlockState rotate(Object state, Rotation rotation) {
        int stateId = BlockStateUtils.blockStateToId(state);
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
        if (immutableBlockState == null || immutableBlockState.isEmpty()) return null;
        Direction facing = immutableBlockState.get(this.facingProperty).toDirection();
        return immutableBlockState.with(this.facingProperty, rotation.rotate(facing).toHorizontalDirection());
    }

    @Override
    public Object mirror(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Mirror mirror = MirrorUtils.fromNMSMirror(args[1]);
        int stateId = BlockStateUtils.blockStateToId(state);
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
        if (immutableBlockState == null || immutableBlockState.isEmpty()) return superMethod.call();
        Direction direction = immutableBlockState.get(this.facingProperty).toDirection();
        StairsShape stairsShape = immutableBlockState.get(this.shapeProperty);
        switch (mirror) {
            case LEFT_RIGHT:
                if (direction.axis() == Direction.Axis.Z) {
                    return switch (stairsShape) {
                        case OUTER_LEFT ->
                                rotate(state, Rotation.CLOCKWISE_180).with(this.shapeProperty, StairsShape.OUTER_RIGHT).customBlockState().handle();
                        case INNER_RIGHT ->
                                rotate(state, Rotation.CLOCKWISE_180).with(this.shapeProperty, StairsShape.INNER_LEFT).customBlockState().handle();
                        case INNER_LEFT ->
                                rotate(state, Rotation.CLOCKWISE_180).with(this.shapeProperty, StairsShape.INNER_RIGHT).customBlockState().handle();
                        case OUTER_RIGHT ->
                                rotate(state, Rotation.CLOCKWISE_180).with(this.shapeProperty, StairsShape.OUTER_LEFT).customBlockState().handle();
                        default -> rotate(state, Rotation.CLOCKWISE_180).customBlockState().handle();
                    };
                }
            case FRONT_BACK:
                if (direction.axis() == Direction.Axis.X) {
                    return switch (stairsShape) {
                        case STRAIGHT -> rotate(state, Rotation.CLOCKWISE_180).customBlockState().handle();
                        case OUTER_LEFT ->
                                rotate(state, Rotation.CLOCKWISE_180).with(this.shapeProperty, StairsShape.OUTER_RIGHT).customBlockState().handle();
                        case INNER_RIGHT ->
                                rotate(state, Rotation.CLOCKWISE_180).with(this.shapeProperty, StairsShape.INNER_RIGHT).customBlockState().handle();
                        case INNER_LEFT -> rotate(state, Rotation.CLOCKWISE_180).with(this.shapeProperty, StairsShape.INNER_LEFT).customBlockState().handle();
                        case OUTER_RIGHT ->
                                rotate(state, Rotation.CLOCKWISE_180).with(this.shapeProperty, StairsShape.OUTER_LEFT).customBlockState().handle();
                    };
                }
        }
        return superMethod.call();
    }

    private StairsShape getStairsShape(ImmutableBlockState state, Object level, BlockPos blockPos) {
        Direction direction = state.get(this.facingProperty).toDirection();
        Object blockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(blockPos.relative(direction)));
        StairsShape straight1 = getStairsShape(state, level, blockPos, blockState, direction, true);
        if (straight1 != null) return straight1;
        Object blockState1 = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, LocationUtils.toBlockPos(blockPos.relative(direction.opposite())));
        StairsShape straight = getStairsShape(state, level, blockPos, blockState1, direction, false);
        if (straight != null) return straight;
        return StairsShape.STRAIGHT;
    }

    @Nullable
    private StairsShape getStairsShape(ImmutableBlockState state, Object level, BlockPos blockPos, Object blockState1, Direction direction, boolean opposite) {
        if (isStairs(blockState1)) {
            int stateId = BlockStateUtils.blockStateToId(blockState1);
            ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
            if (immutableBlockState == null || immutableBlockState.isEmpty()) return StairsShape.STRAIGHT;
            if (state.get(this.facingProperty) == immutableBlockState.get(this.facingProperty)) {
                Direction direction1 = immutableBlockState.get(this.facingProperty).toDirection();
                if (direction1.axis() != state.get(this.facingProperty).toDirection().axis() && canTakeShape(state, level, blockPos, opposite ? direction1.opposite() : direction1)) {
                    if (direction1 == direction.counterClockWise()) {
                        return opposite ? StairsShape.OUTER_LEFT : StairsShape.INNER_LEFT;
                    }
                    return opposite ? StairsShape.OUTER_RIGHT : StairsShape.INNER_LEFT;
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
        Optional<StairsBlockBehavior> optionalBehavior = immutableBlockState.behavior().getAs(StairsBlockBehavior.class);
        return optionalBehavior.isPresent();
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
