package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.MirrorUtils;
import net.momirealms.craftengine.bukkit.util.RotationUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.AbstractBlockBehavior;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.Mirror;
import net.momirealms.craftengine.core.util.Rotation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

public class BukkitBlockBehavior extends AbstractBlockBehavior {
    private static final Map<String, BiConsumer<BukkitBlockBehavior, Property<?>>> HARD_CODED_PROPERTY_DATA = new HashMap<>();
    static {
        HARD_CODED_PROPERTY_DATA.put("axis", (behavior, property) -> {
            @SuppressWarnings("unchecked")
            Property<Direction.Axis> axisProperty = (Property<Direction.Axis>) property;
            behavior.rotateFunction = (thisBlock, blockState, rotation) -> {
                Direction.Axis axis = blockState.get(axisProperty);
                return switch (rotation) {
                    case COUNTERCLOCKWISE_90, CLOCKWISE_90 -> switch (axis) {
                        case X -> blockState.with(axisProperty, Direction.Axis.Z).customBlockState().handle();
                        case Z -> blockState.with(axisProperty, Direction.Axis.X).customBlockState().handle();
                        default -> blockState.customBlockState().handle();
                    };
                    default -> blockState.customBlockState().handle();
                };
            };
        });
        HARD_CODED_PROPERTY_DATA.put("facing", (behavior, property) -> {
            if (property.valueClass() == HorizontalDirection.class) {
                @SuppressWarnings("unchecked")
                Property<HorizontalDirection> directionProperty = (Property<HorizontalDirection>) property;
                behavior.rotateFunction = (thisBlock, blockState, rotation) ->
                        blockState.with(directionProperty, rotation.rotate(blockState.get(directionProperty).toDirection()).toHorizontalDirection())
                                .customBlockState().handle();
                behavior.mirrorFunction = (thisBlock, blockState, mirror) -> {
                    Rotation rotation = mirror.getRotation(blockState.get(directionProperty).toDirection());
                    return behavior.rotateFunction.rotate(thisBlock, blockState, rotation);
                };
            } else if (property.valueClass() == Direction.class) {
                @SuppressWarnings("unchecked")
                Property<Direction> directionProperty = (Property<Direction>) property;
                behavior.rotateFunction = (thisBlock, blockState, rotation) ->
                        blockState.with(directionProperty, rotation.rotate(blockState.get(directionProperty)))
                                .customBlockState().handle();
                behavior.mirrorFunction = (thisBlock, blockState, mirror) -> {
                    Rotation rotation = mirror.getRotation(blockState.get(directionProperty));
                    return behavior.rotateFunction.rotate(thisBlock, blockState, rotation);
                };
            }
        });
        HARD_CODED_PROPERTY_DATA.put("facing_clockwise", (behavior, property) -> {
            if (property.valueClass() == HorizontalDirection.class) {
                @SuppressWarnings("unchecked")
                Property<HorizontalDirection> directionProperty = (Property<HorizontalDirection>) property;
                behavior.rotateFunction = (thisBlock, blockState, rotation) ->
                        blockState.with(directionProperty, rotation.rotate(blockState.get(directionProperty).toDirection()).toHorizontalDirection())
                                .customBlockState().handle();
                behavior.mirrorFunction = (thisBlock, blockState, mirror) -> {
                    Rotation rotation = mirror.getRotation(blockState.get(directionProperty).toDirection());
                    return behavior.rotateFunction.rotate(thisBlock, blockState, rotation);
                };
            }
        });
    }

    private MirrorFunction mirrorFunction;
    private RotateFunction rotateFunction;

    public BukkitBlockBehavior(CustomBlock customBlock) {
        super(customBlock);
        for (Property<?> property : customBlock.properties()) {
            Optional.ofNullable(HARD_CODED_PROPERTY_DATA.get(property.name())).ifPresent(
                    c -> c.accept(this, property)
            );
        }
    }

    @Override
    public Object mirror(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        if (this.mirrorFunction != null) {
            int id = BlockStateUtils.blockStateToId(args[0]);
            ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(id);
            return this.mirrorFunction.mirror(thisBlock, state, MirrorUtils.fromNMSMirror(args[1]));
        }
        return super.mirror(thisBlock, args, superMethod);
    }

    @Override
    public Object rotate(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        if (this.rotateFunction != null) {
            int id = BlockStateUtils.blockStateToId(args[0]);
            ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(id);
            return this.rotateFunction.rotate(thisBlock, state, RotationUtils.fromNMSRotation(args[1]));
        }
        return super.rotate(thisBlock, args, superMethod);
    }

    @FunctionalInterface
    interface MirrorFunction {

        Object mirror(Object thisBlock, ImmutableBlockState state, Mirror mirror) throws Exception;
    }

    @FunctionalInterface
    interface RotateFunction {

        Object rotate(Object thisBlock, ImmutableBlockState state, Rotation rotation) throws Exception;
    }
}
