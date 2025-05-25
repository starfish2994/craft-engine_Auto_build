package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.IntegerProperty;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.util.RandomUtils;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.WorldEvents;
import net.momirealms.craftengine.core.world.WorldPosition;
import net.momirealms.craftengine.shared.block.BlockBehavior;

import java.util.Map;
import java.util.concurrent.Callable;

public class VerticalCropBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final int maxHeight;
    private final IntegerProperty ageProperty;
    private final float growSpeed;
    private final boolean direction;

    public VerticalCropBlockBehavior(CustomBlock customBlock, Property<Integer> ageProperty,
                                     int maxHeight, float growSpeed, boolean direction) {
        super(customBlock);
        this.maxHeight = maxHeight;
        this.ageProperty = (IntegerProperty) ageProperty;
        this.growSpeed = growSpeed;
        this.direction = direction;
    }

    @Override
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object blockState = args[0];
        Object level = args[1];
        Object blockPos = args[2];
        if (!canSurvive(thisBlock, args, () -> true)) {
            int stateId = BlockStateUtils.blockStateToId(blockState);
            ImmutableBlockState currentState = BukkitBlockManager.instance().getImmutableBlockState(stateId);
            if (currentState != null && !currentState.isEmpty()) {
                // break the crop
                FastNMS.INSTANCE.method$Level$removeBlock(level, blockPos, false);
                net.momirealms.craftengine.core.world.World world = new BukkitWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(level));
                WorldPosition position = new WorldPosition(world, Vec3d.atCenterOf(LocationUtils.fromBlockPos(blockPos)));
                ContextHolder.Builder builder = ContextHolder.builder()
                        .withParameter(DirectContextParameters.POSITION, position);
                for (Item<Object> item : currentState.getDrops(builder, world, null)) {
                    world.dropItemNaturally(position, item);
                }
                world.playBlockSound(position, currentState.sounds().breakSound());
                FastNMS.INSTANCE.method$Level$levelEvent(level, WorldEvents.BLOCK_BREAK_EFFECT, blockPos, stateId);
            }
        }
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object world;
        Object blockPos;
        if (VersionHelper.isOrAbove1_21_2()) {
            world = args[1];
            blockPos = args[3];
        } else {
            world = args[3];
            blockPos = args[4];
        }
        Reflections.method$LevelAccessor$scheduleTick.invoke(world, blockPos, thisBlock, 1);
        // return state, do not call super.
        return args[0];
    }

    @Override
    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object blockState = args[0];
        Object level = args[1];
        Object blockPos = args[2];
        // above block is empty
        if (FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, (this.direction ? LocationUtils.above(blockPos) : LocationUtils.below(blockPos))) == Reflections.instance$Blocks$AIR$defaultState) {
            int currentHeight = 1;
            BlockPos currentPos = LocationUtils.fromBlockPos(blockPos);
            ImmutableBlockState currentState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
            if (currentState != null && !currentState.isEmpty()) {
                while (true) {
                    Object nextPos = LocationUtils.toBlockPos(currentPos.x(), this.direction ? currentPos.y() - currentHeight : currentPos.y() + currentHeight, currentPos.z());
                    Object nextState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, nextPos);
                    ImmutableBlockState belowImmutableState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(nextState));
                    if (belowImmutableState != null && !belowImmutableState.isEmpty() && belowImmutableState.owner() == currentState.owner()) {
                        currentHeight++;
                    } else {
                        break;
                    }
                }
            } else {
                return;
            }

            if (currentHeight < this.maxHeight) {
                int age = currentState.get(ageProperty);
                if (age >= this.ageProperty.max || RandomUtils.generateRandomFloat(0, 1) < this.growSpeed) {
                    Object nextPos = this.direction ? LocationUtils.above(blockPos) : LocationUtils.below(blockPos);
                    if (VersionHelper.isOrAbove1_21_5()) {
                        Reflections.method$CraftEventFactory$handleBlockGrowEvent.invoke(null, level, nextPos, super.customBlock.defaultState().customBlockState().handle(), UpdateOption.UPDATE_ALL.flags());
                    } else {
                        Reflections.method$CraftEventFactory$handleBlockGrowEvent.invoke(null, level, nextPos, super.customBlock.defaultState().customBlockState().handle());
                    }
                    FastNMS.INSTANCE.method$LevelWriter$setBlock(level, blockPos, currentState.with(this.ageProperty, this.ageProperty.min).customBlockState().handle(), UpdateOption.UPDATE_NONE.flags());
                } else if (RandomUtils.generateRandomFloat(0, 1) < this.growSpeed) {
                    FastNMS.INSTANCE.method$LevelWriter$setBlock(level, blockPos, currentState.with(this.ageProperty, age + 1).customBlockState().handle(), UpdateOption.UPDATE_NONE.flags());
                }
            }
        }
    }

    public static class Factory implements BlockBehaviorFactory {

        @SuppressWarnings("unchecked")
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Integer> ageProperty = (Property<Integer>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("age"), "warning.config.block.behavior.sugar_cane.missing_age");
            int maxHeight = ResourceConfigUtils.getAsInt(arguments.getOrDefault("max-height", 3), "max-height");
            boolean direction = arguments.getOrDefault("direction", "up").toString().equalsIgnoreCase("up");
            return new VerticalCropBlockBehavior(block, ageProperty, maxHeight,
                    ResourceConfigUtils.getAsFloat(arguments.getOrDefault("grow-speed", 1), "grow-speed"), direction);
        }
    }
}
