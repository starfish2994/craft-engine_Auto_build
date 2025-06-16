package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Half;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;

public class TrapDoorBlockBehavior extends WaterLoggedBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<Half> halfProperty;
    private final Property<HorizontalDirection> directionProperty;
    private final Property<Boolean> poweredProperty;
    private final Property<Boolean> openProperty;

    public TrapDoorBlockBehavior(CustomBlock block,
                                 @Nullable Property<Boolean> waterloggedProperty,
                                 Property<Half> halfProperty,
                                 Property<HorizontalDirection> directionProperty,
                                 Property<Boolean> poweredProperty,
                                 Property<Boolean> openProperty) {
        super(block, waterloggedProperty);
        this.halfProperty = halfProperty;
        this.directionProperty = directionProperty;
        this.poweredProperty = poweredProperty;
        this.openProperty = openProperty;
    }
//
//    @Override
//    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
//
//    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {

    }

    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {

    }

    @SuppressWarnings("unchecked")
    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Boolean> waterlogged = (Property<Boolean>) block.getProperty("waterlogged");
            Property<Half> half = (Property<Half>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("half"), "warning.config.block.behavior.trapdoor.missing_half");
            Property<HorizontalDirection> direction = (Property<HorizontalDirection>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("direction"), "warning.config.block.behavior.trapdoor.missing_direction");
            Property<Boolean> open = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("open"), "warning.config.block.behavior.trapdoor.missing_open");
            Property<Boolean> powered = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("open"), "warning.config.block.behavior.trapdoor.missing_powered");
            return new TrapDoorBlockBehavior(block, waterlogged, half, direction, powered, open);
        }
    }
}
