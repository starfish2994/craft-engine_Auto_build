package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.block.state.properties.DoorHinge;
import net.momirealms.craftengine.core.block.state.properties.DoubleBlockHalf;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.plugin.context.ContextHolder;
import net.momirealms.craftengine.core.plugin.context.parameter.DirectContextParameters;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.HorizontalDirection;
import net.momirealms.craftengine.core.util.ResourceConfigUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.*;
import org.bukkit.Bukkit;
import org.bukkit.GameEvent;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

public class DoorBlockBehavior extends AbstractCanSurviveBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<DoubleBlockHalf> halfProperty;
    private final Property<HorizontalDirection> facingProperty;
    private final Property<DoorHinge> hingeProperty;
    private final Property<Boolean> poweredProperty;
    private final Property<Boolean> openProperty;
    private final boolean canOpenWithHand;
    private final boolean canOpenByWindCharge;
    private final SoundData openSound;
    private final SoundData closeSound;

    public DoorBlockBehavior(CustomBlock block,
                             Property<DoubleBlockHalf> halfProperty,
                             Property<HorizontalDirection> facingProperty,
                             Property<DoorHinge> hingeProperty,
                             Property<Boolean> poweredProperty,
                             Property<Boolean> openProperty,
                             boolean canOpenWithHand,
                             boolean canOpenByWindCharge,
                             SoundData openSound,
                             SoundData closeSound) {
        super(block, 0);
        this.halfProperty = halfProperty;
        this.facingProperty = facingProperty;
        this.hingeProperty = hingeProperty;
        this.poweredProperty = poweredProperty;
        this.openProperty = openProperty;
        this.canOpenWithHand = canOpenWithHand;
        this.canOpenByWindCharge = canOpenByWindCharge;
        this.openSound = openSound;
        this.closeSound = closeSound;
    }

    public boolean isOpen(ImmutableBlockState state) {
        return state.get(this.openProperty);
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
        DoubleBlockHalf half = immutableBlockState.get(this.halfProperty);
        Object direction = VersionHelper.isOrAbove1_21_2() ? args[4] : args[0];
        if (DirectionUtils.isYAxis(direction) && half == DoubleBlockHalf.LOWER == (direction == CoreReflections.instance$Direction$UP)) {
            ImmutableBlockState neighborState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(VersionHelper.isOrAbove1_21_2() ? args[6] : args[2]));
            if (neighborState == null || neighborState.isEmpty()) {
                return MBlocks.AIR$defaultState;
            }
            Optional<DoorBlockBehavior> anotherDoorBehavior = neighborState.behavior().getAs(DoorBlockBehavior.class);
            if (anotherDoorBehavior.isEmpty()) {
                return MBlocks.AIR$defaultState;
            }
            if (neighborState.get(anotherDoorBehavior.get().halfProperty) != half) {
                return neighborState.with(anotherDoorBehavior.get().halfProperty, half).customBlockState().handle();
            }
            return MBlocks.AIR$defaultState;
        } else {
            if (half == DoubleBlockHalf.LOWER && direction == CoreReflections.instance$Direction$DOWN
                    && !canSurvive(thisBlock, blockState, level, blockPos)) {
                BlockPos pos = LocationUtils.fromBlockPos(blockPos);
                net.momirealms.craftengine.core.world.World world = new BukkitWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(level));
                WorldPosition position = new WorldPosition(world, Vec3d.atCenterOf(pos));
                ContextHolder.Builder builder = ContextHolder.builder()
                        .withParameter(DirectContextParameters.POSITION, position);
                for (Item<Object> item : immutableBlockState.getDrops(builder, world, null)) {
                    world.dropItemNaturally(position, item);
                }
                world.playBlockSound(position, immutableBlockState.sounds().breakSound());
                FastNMS.INSTANCE.method$Level$levelEvent(level, WorldEvents.BLOCK_BREAK_EFFECT, blockPos, stateId);
                return MBlocks.AIR$defaultState;
            }
            return blockState;
        }
    }

    @Override
    public void onExplosionHit(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (this.canOpenByWindCharge && FastNMS.INSTANCE.method$Explosion$canTriggerBlocks(args[3])) {
            ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(args[0]));
            if (state == null || state.isEmpty()) return;
            if (state.get(this.poweredProperty)) return;
            if (state.get(this.halfProperty) == DoubleBlockHalf.LOWER) {
                this.setOpen(null, args[1], state, LocationUtils.fromBlockPos(args[2]), !this.isOpen(state));
            }
        }
    }

    @Override
    public void setPlacedBy(BlockPlaceContext context, ImmutableBlockState state) {
        BlockPos pos = context.getClickedPos();
        context.getLevel().setBlockAt(pos.x(), pos.y() + 1, pos.z(), state.with(this.halfProperty, DoubleBlockHalf.UPPER).customBlockState(), UpdateOption.UPDATE_ALL.flags());
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        World world  = context.getLevel();
        Object level = world.serverWorld();
        BlockPos pos = context.getClickedPos();
        if (pos.y() < context.getLevel().worldHeight().getMaxBuildHeight() && world.getBlockAt(pos.above()).canBeReplaced(context)) {
            boolean hasSignal = FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, LocationUtils.toBlockPos(pos)) || FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, LocationUtils.toBlockPos(pos.above()));
            return state.with(this.poweredProperty, hasSignal)
                    .with(this.facingProperty, context.getHorizontalDirection().toHorizontalDirection())
                    .with(this.openProperty, hasSignal)
                    .with(this.halfProperty, DoubleBlockHalf.LOWER)
                    .with(this.hingeProperty, getHinge(context));
        }
        return null;
    }

    private DoorHinge getHinge(BlockPlaceContext context) {
        Object serverLevel = context.getLevel().serverWorld();
        BlockPos clickedPos = context.getClickedPos();
        Direction horizontalDirection = context.getHorizontalDirection();
        BlockPos blockPos = clickedPos.above();

        Direction counterClockWise = horizontalDirection.counterClockWise();
        Object blockPos1 = LocationUtils.toBlockPos(clickedPos.relative(counterClockWise));
        Object blockState1 = FastNMS.INSTANCE.method$BlockGetter$getBlockState(serverLevel, blockPos1);
        Object blockPos2 = LocationUtils.toBlockPos(blockPos.relative(counterClockWise));
        Object blockState2 = FastNMS.INSTANCE.method$BlockGetter$getBlockState(serverLevel, blockPos2);

        Direction clockWise = horizontalDirection.clockWise();
        Object blockPos3 = LocationUtils.toBlockPos(clickedPos.relative(clockWise));
        Object blockState3 = FastNMS.INSTANCE.method$BlockGetter$getBlockState(serverLevel, blockPos3);
        Object blockPos4 = LocationUtils.toBlockPos(blockPos.relative(clockWise));
        Object blockState4 = FastNMS.INSTANCE.method$BlockGetter$getBlockState(serverLevel, blockPos4);

        int i = (FastNMS.INSTANCE.method$BlockStateBase$isCollisionShapeFullBlock(blockState1, serverLevel, blockPos1) ? -1 : 0) +
                (FastNMS.INSTANCE.method$BlockStateBase$isCollisionShapeFullBlock(blockState2, serverLevel, blockPos2) ? -1 : 0) +
                (FastNMS.INSTANCE.method$BlockStateBase$isCollisionShapeFullBlock(blockState3, serverLevel, blockPos3) ? 1 : 0) +
                (FastNMS.INSTANCE.method$BlockStateBase$isCollisionShapeFullBlock(blockState4, serverLevel, blockPos4) ? 1 : 0);

        boolean anotherDoor1 = isAnotherDoor(blockState1);
        boolean anotherDoor2 = isAnotherDoor(blockState3);
        if ((!anotherDoor1 || anotherDoor2) && i <= 0) {
            if ((!anotherDoor2 || anotherDoor1) && i == 0) {
                int stepX = horizontalDirection.stepX();
                int stepZ = horizontalDirection.stepZ();
                Vec3d clickLocation = context.getClickLocation();
                double d = clickLocation.x - (double) clickedPos.x();
                double d1 = clickLocation.z - (double) clickedPos.z();
                return stepX < 0 && d1 < (double) 0.5F || stepX > 0 && d1 > (double) 0.5F || stepZ < 0 && d > (double) 0.5F || stepZ > 0 && d < (double) 0.5F ? DoorHinge.RIGHT : DoorHinge.LEFT;
            } else {
                return DoorHinge.LEFT;
            }
        } else {
            return DoorHinge.RIGHT;
        }
    }

    private boolean isAnotherDoor(Object blockState) {
        int id = BlockStateUtils.blockStateToId(blockState);
        if (BlockStateUtils.isVanillaBlock(id)) {
            BlockData blockData = BlockStateUtils.fromBlockData(blockState);
            return blockData instanceof Door door && door.getHalf() == Bisected.Half.BOTTOM;
        } else {
            ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockStateUnsafe(id);
            if (state.isEmpty()) return false;
            Optional<DoorBlockBehavior> optional = state.behavior().getAs(DoorBlockBehavior.class);
            return optional.isPresent() && state.get(optional.get().halfProperty) == DoubleBlockHalf.LOWER;
        }
    }

    public void setOpen(@Nullable Player player, Object serverLevel, ImmutableBlockState state, BlockPos pos, boolean isOpen) {
        if (isOpen(state) != isOpen) {
            org.bukkit.World world = FastNMS.INSTANCE.method$Level$getCraftWorld(serverLevel);
            FastNMS.INSTANCE.method$LevelWriter$setBlock(serverLevel, LocationUtils.toBlockPos(pos), state.with(this.openProperty, isOpen).customBlockState().handle(), UpdateOption.builder().updateImmediate().updateClients().build().flags());
            world.sendGameEvent(player == null ? null : (org.bukkit.entity.Player) player.platformPlayer(), isOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, new Vector(pos.x(), pos.y(), pos.z()));
            SoundData soundData = isOpen ? this.openSound : this.closeSound;
            if (soundData != null) {
                new BukkitWorld(world).playBlockSound(
                        new Vec3d(pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5),
                        soundData
                );
            }
        }
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        if (!this.canOpenWithHand) {
            return InteractionResult.PASS;
        }
        setOpen(context.getPlayer(), context.getLevel().serverWorld(), state, context.getClickedPos(), !state.get(this.openProperty));
        return InteractionResult.SUCCESS_AND_CANCEL;
    }

    @Override
    public boolean isPathFindable(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object type = VersionHelper.isOrAbove1_20_5() ? args[1] : args[3];
        Object blockState = args[0];
        ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (state == null || state.isEmpty()) return false;
        if (type == CoreReflections.instance$PathComputationType$LAND || type == CoreReflections.instance$PathComputationType$AIR) {
            return state.get(this.openProperty);
        }
        return false;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object blockPos = args[2];
        Object level = args[1];
        Object blockState = args[0];
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (immutableBlockState == null || immutableBlockState.isEmpty()) return;
        Object anotherHalfPos = immutableBlockState.get(this.halfProperty) == DoubleBlockHalf.LOWER ? LocationUtils.above(blockPos) : LocationUtils.below(blockPos);
        Block bukkitBlock = FastNMS.INSTANCE.method$CraftBlock$at(level, blockPos);
        Block anotherBukkitBlock = FastNMS.INSTANCE.method$CraftBlock$at(level, anotherHalfPos);
        int power = Math.max(bukkitBlock.getBlockPower(), anotherBukkitBlock.getBlockPower());
        int oldPower = immutableBlockState.get(this.poweredProperty) ? 15 : 0;
        if (oldPower == 0 ^ power == 0) {
            BlockRedstoneEvent event = new BlockRedstoneEvent(bukkitBlock, oldPower, power);
            Bukkit.getPluginManager().callEvent(event);
            boolean flag = event.getNewCurrent() > 0;
            if (flag != immutableBlockState.get(this.openProperty)) {
                org.bukkit.World world = FastNMS.INSTANCE.method$Level$getCraftWorld(level);
                world.sendGameEvent(null, flag ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, new Vector(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ()));
                SoundData soundData = flag ? this.openSound : this.closeSound;
                if (soundData != null) {
                    new BukkitWorld(world).playBlockSound(
                            new Vec3d(FastNMS.INSTANCE.field$Vec3i$x(blockPos) + 0.5, FastNMS.INSTANCE.field$Vec3i$y(blockPos) + 0.5, FastNMS.INSTANCE.field$Vec3i$z(blockPos) + 0.5),
                            soundData
                    );
                }
            }
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, blockPos, immutableBlockState.with(this.poweredProperty, flag).with(this.openProperty, flag).customBlockState().handle(), UpdateOption.Flags.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canSurvive(Object thisBlock, Object state, Object world, Object blockPos) throws Exception {
        ImmutableBlockState customBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (customBlockState == null || customBlockState.isEmpty()) return false;
        int x = FastNMS.INSTANCE.field$Vec3i$x(blockPos);
        int y = FastNMS.INSTANCE.field$Vec3i$y(blockPos) - 1;
        int z = FastNMS.INSTANCE.field$Vec3i$z(blockPos);
        Object belowPos = FastNMS.INSTANCE.constructor$BlockPos(x, y, z);
        Object belowState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(world, belowPos);
        if (customBlockState.get(this.halfProperty) == DoubleBlockHalf.UPPER) {
            ImmutableBlockState belowCustomState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(belowState));
            if (belowCustomState == null || belowCustomState.isEmpty()) return false;
            return belowCustomState.owner().value() == super.customBlock;
        } else {
            return (boolean) CoreReflections.method$BlockStateBase$isFaceSturdy.invoke(
                    belowState, world, belowPos, CoreReflections.instance$Direction$UP,
                    CoreReflections.instance$SupportType$FULL
            );
        }
    }

    @SuppressWarnings("unchecked")
    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<DoubleBlockHalf> half = (Property<DoubleBlockHalf>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("half"), "warning.config.block.behavior.door.missing_half");
            Property<HorizontalDirection> facing = (Property<HorizontalDirection>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.door.missing_facing");
            Property<DoorHinge> hinge = (Property<DoorHinge>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("hinge"), "warning.config.block.behavior.door.missing_hinge");
            Property<Boolean> open = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("open"), "warning.config.block.behavior.door.missing_open");
            Property<Boolean> powered = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("powered"), "warning.config.block.behavior.door.missing_powered");
            boolean canOpenWithHand = (boolean) arguments.getOrDefault("can-open-with-hand", true);
            boolean canOpenByWindCharge = (boolean) arguments.getOrDefault("can-open-by-wind-charge", true);
            Map<String, Object> sounds = (Map<String, Object>) arguments.get("sounds");
            SoundData openSound = null;
            SoundData closeSound = null;
            if (sounds != null) {
                openSound = Optional.ofNullable(sounds.get("open")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
                closeSound = Optional.ofNullable(sounds.get("close")).map(obj -> SoundData.create(obj, SoundData.SoundValue.FIXED_1, SoundData.SoundValue.ranged(0.9f, 1f))).orElse(null);
            }
            return new DoorBlockBehavior(block, half, facing, hinge, powered, open, canOpenWithHand, canOpenByWindCharge, openSound, closeSound);
        }
    }
}
