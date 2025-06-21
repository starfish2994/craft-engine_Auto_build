package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorldManager;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.PressurePlateSensitivity;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.GameEvent;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class PressurePlateBlockBehavior extends BukkitBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<Boolean> poweredProperty;
    private final SoundData onSound;
    private final SoundData offSound;
    private final PressurePlateSensitivity pressurePlateSensitivity;

    public PressurePlateBlockBehavior(
            CustomBlock block,
            Property<Boolean> poweredProperty,
            SoundData onSound,
            SoundData offSound,
            PressurePlateSensitivity pressurePlateSensitivity
    ) {
        super(block);
        this.poweredProperty = poweredProperty;
        this.onSound = onSound;
        this.offSound = offSound;
        this.pressurePlateSensitivity = pressurePlateSensitivity;
    }

    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        Object level;
        Object blockPos;
        if (VersionHelper.isOrAbove1_21_2()) {
            level = args[1];
            blockPos = args[3];
        } else {
            level = args[3];
            blockPos = args[4];
        }
        Direction direction = DirectionUtils.fromNMSDirection(VersionHelper.isOrAbove1_21_2() ? args[4] : args[0]);
        return direction == Direction.DOWN && !FastNMS.INSTANCE.method$BlockStateBase$canSurvive(state, level, blockPos)
                ? MBlocks.AIR$defaultState
                : superMethod.call();
    }

    public boolean canSurvive(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object blockPos = LocationUtils.fromVec(args[2]);
        Object level = args[1];
        return FastNMS.INSTANCE.method$Block$canSupportRigidBlock(level, blockPos)
                || FastNMS.INSTANCE.method$Block$canSupportCenter(level, blockPos, CoreReflections.instance$Direction$UP);
    }

    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        Object state = args[0];
        int signalForState = this.getSignalForState(state);
        if (signalForState > 0) {
            this.checkPressed(null, args[1], args[2], state, signalForState, thisBlock);
        }
    }

    public void entityInside(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object state = args[0];
        int signalForState = this.getSignalForState(state);
        if (signalForState == 0) {
            this.checkPressed(args[3], args[1], args[2], state, signalForState, thisBlock);
        }
    }

    protected int getSignalStrength(Object level, Object pos) {
        Class<?> clazz = switch (this.pressurePlateSensitivity) {
            case EVERYTHING -> CoreReflections.clazz$Entity;
            case MOBS -> CoreReflections.clazz$LivingEntity;
        };
        Object box = FastNMS.INSTANCE.method$AABB$move(CoreReflections.instance$BasePressurePlateBlock$TOUCH_AABB, pos);
        return FastNMS.INSTANCE.method$BasePressurePlateBlock$getEntityCount(level, box, clazz) > 0 ? 15 : 0;
    }

    private Object setSignalForState(Object state, int strength) {
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return state;
        return blockState.with(this.poweredProperty, strength > 0).customBlockState().handle();
    }

    private void checkPressed(@Nullable Object entity, Object level, Object pos, Object state, int currentSignal, Object thisBlock) {
        int signalStrength = this.getSignalStrength(level, pos);
        boolean flag = currentSignal > 0;
        boolean flag1 = signalStrength > 0;
        if (currentSignal != signalStrength) {
            Object blockState = this.setSignalForState(state, signalStrength);
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, blockState, 2);
            this.updateNeighbours(level, pos, thisBlock);
            FastNMS.INSTANCE.method$Level$setBlocksDirty(level, pos, state, blockState);
        }

        if (!flag1 && flag) {
            World world = BukkitWorldManager.instance().getWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(level)).world();
            world.playBlockSound(LocationUtils.toVec3d(LocationUtils.fromBlockPos(pos)), this.offSound);
            FastNMS.INSTANCE.method$Level$getCraftWorld(level).sendGameEvent(FastNMS.INSTANCE.method$Entity$getBukkitEntity(entity),
                    GameEvent.BLOCK_DEACTIVATE,
                    new Vector(FastNMS.INSTANCE.field$Vec3i$x(pos), FastNMS.INSTANCE.field$Vec3i$y(pos), FastNMS.INSTANCE.field$Vec3i$z(pos))
            );
        } else if (flag1 && !flag) {
            World world = BukkitWorldManager.instance().getWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(level)).world();
            world.playBlockSound(LocationUtils.toVec3d(LocationUtils.fromBlockPos(pos)), this.onSound);
            FastNMS.INSTANCE.method$Level$getCraftWorld(level).sendGameEvent(FastNMS.INSTANCE.method$Entity$getBukkitEntity(entity),
                    GameEvent.BLOCK_ACTIVATE,
                    new Vector(FastNMS.INSTANCE.field$Vec3i$x(pos), FastNMS.INSTANCE.field$Vec3i$y(pos), FastNMS.INSTANCE.field$Vec3i$z(pos))
            );
        }

        if (flag1) {
            FastNMS.INSTANCE.method$LevelAccessor$scheduleBlockTick(level, pos, thisBlock, 20);
        }
    }

    public void affectNeighborsAfterRemoval(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        boolean movedByPiston = (boolean) args[3];
        if (!movedByPiston && this.getSignalForState(args[0]) > 0) {
            this.updateNeighbours(args[1], args[2], thisBlock);
        }
    }

    private void updateNeighbours(Object level, Object pos, Object thisBlock) {
        FastNMS.INSTANCE.method$Level$updateNeighborsAt(level, pos, thisBlock);
        FastNMS.INSTANCE.method$Level$updateNeighborsAt(level, LocationUtils.below(pos), thisBlock);
    }

    public int getSignal(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        return this.getSignalForState(args[0]);
    }

    private int getSignalForState(Object state) {
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return 0;
        return blockState.get(this.poweredProperty) ? 15 : 0;
    }

    public int getDirectSignal(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Direction direction = DirectionUtils.fromNMSDirection(args[3]);
        return direction == Direction.UP ? this.getSignalForState(args[0]) : 0;
    }

    public boolean isSignalSource(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        return true;
    }

    public static class Factory implements BlockBehaviorFactory {

        @SuppressWarnings("unchecked")
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Boolean> powered = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("powered"), "warning.config.block.behavior.pressure_plate.missing_powered");
            PressurePlateSensitivity pressurePlateSensitivity = PressurePlateSensitivity.byName(arguments.getOrDefault("pressure-plate-sensitivity", "everything").toString());
            Map<String, Object> sounds = (Map<String, Object>) arguments.get("sounds");
            SoundData onSound = null;
            SoundData offSound = null;
            if (sounds != null) {
                onSound = Optional.ofNullable(sounds.get("on")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
                offSound = Optional.ofNullable(sounds.get("off")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
            }
            return new PressurePlateBlockBehavior(block, powered, onSound, offSound, pressurePlateSensitivity);
        }
    }
}
