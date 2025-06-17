package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MBlocks;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.MFluids;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.LocationUtils;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.block.BlockBehavior;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.block.behavior.BlockBehaviorFactory;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.block.state.properties.SingleBlockHalf;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.ItemKeys;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.util.*;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.Bukkit;
import org.bukkit.GameEvent;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.Callable;

public class TrapDoorBlockBehavior extends WaterLoggedBlockBehavior {
    public static final Factory FACTORY = new Factory();
    private final Property<SingleBlockHalf> halfProperty;
    private final Property<HorizontalDirection> facingProperty;
    private final Property<Boolean> poweredProperty;
    private final Property<Boolean> openProperty;
    private final boolean canOpenWithHand;
    private final boolean canOpenByWindCharge;

    public TrapDoorBlockBehavior(CustomBlock block,
                                 @Nullable Property<Boolean> waterloggedProperty,
                                 Property<SingleBlockHalf> halfProperty,
                                 Property<HorizontalDirection> facingProperty,
                                 Property<Boolean> poweredProperty,
                                 Property<Boolean> openProperty,
                                 boolean canOpenWithHand,
                                 boolean canOpenByWindCharge) {
        super(block, waterloggedProperty);
        this.halfProperty = halfProperty;
        this.facingProperty = facingProperty;
        this.poweredProperty = poweredProperty;
        this.openProperty = openProperty;
        this.canOpenWithHand = canOpenWithHand;
        this.canOpenByWindCharge = canOpenByWindCharge;
    }

    @Override
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object blockState = args[0];
        if (this.waterloggedProperty != null) {
            ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
            if (state != null && !state.isEmpty() && state.get(this.waterloggedProperty)) {
                FastNMS.INSTANCE.method$LevelAccessor$scheduleFluidTick(VersionHelper.isOrAbove1_21_2() ? args[2] : args[3], VersionHelper.isOrAbove1_21_2() ? args[3] : args[4], MFluids.WATER, 5);
            }
        }
        return blockState;
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        Object level = context.getLevel().serverWorld();
        Object clickedPos = LocationUtils.toBlockPos(context.getClickedPos());
        Direction clickedFace = context.getClickedFace();
        if (!context.replacingClickedOnBlock() && clickedFace.axis().isHorizontal()) {
            state = state.with(this.facingProperty, clickedFace.toHorizontalDirection())
                    .with(this.halfProperty, context.getClickLocation().y - context.getClickedPos().y() > 0.5 ? SingleBlockHalf.TOP : SingleBlockHalf.BOTTOM);
        } else {
            state = state.with(this.facingProperty, context.getHorizontalDirection().opposite().toHorizontalDirection())
                    .with(this.halfProperty, clickedFace == Direction.UP ? SingleBlockHalf.BOTTOM : SingleBlockHalf.TOP);
        }
        if (FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, clickedPos)) {
            state = state.with(this.poweredProperty, true);
        }
        if (this.waterloggedProperty != null && FastNMS.INSTANCE.method$FluidState$getType(FastNMS.INSTANCE.method$Level$getFluidState(level, clickedPos)) == MFluids.WATER) {
            state = state.with(this.waterloggedProperty, true);
        }
        return state;
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        if (!this.canOpenWithHand) {
            return InteractionResult.PASS;
        }
        this.toggle(state, context.getLevel(), context.getClickedPos(), context.getPlayer());
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean isPathFindable(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object type = VersionHelper.isOrAbove1_20_5() ? args[1] : args[3];
        Object blockState = args[0];
        ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (state == null || state.isEmpty()) return false;
        if (type == CoreReflections.instance$PathComputationType$LAND || type == CoreReflections.instance$PathComputationType$AIR) {
            return state.get(this.openProperty);
        } else if (type == CoreReflections.instance$PathComputationType$WATER) {
            return state.get(super.waterloggedProperty);
        }
        return false;
    }

    @Override
    public void onExplosionHit(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        if (this.canOpenByWindCharge && FastNMS.INSTANCE.method$Explosion$canTriggerBlocks(args[3])) {
            ImmutableBlockState state = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(args[0]));
            if (state == null || state.isEmpty()) return;
            this.toggle(state, new BukkitWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(args[1])), LocationUtils.fromBlockPos(args[2]), null);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object blockState = args[0];
        ImmutableBlockState immutableBlockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState));
        if (immutableBlockState == null || immutableBlockState.isEmpty()) return;
        Object level = args[1];
        Object blockPos = args[2];
        boolean hasSignal = FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, blockPos);
        if (hasSignal == immutableBlockState.get(this.poweredProperty)) return;

        Block bblock = FastNMS.INSTANCE.method$CraftBlock$at(level, blockPos);
        int power = bblock.getBlockPower();
        int oldPower = immutableBlockState.get(this.openProperty) ? 15 : 0;
        Object neighborBlock = args[3];

        if (oldPower == 0 ^ power == 0 || FastNMS.INSTANCE.method$BlockStateBase$isSignalSource(FastNMS.INSTANCE.method$Block$defaultState(neighborBlock))) {
            BlockRedstoneEvent event = new BlockRedstoneEvent(bblock, oldPower, power);
            Bukkit.getPluginManager().callEvent(event);
            hasSignal = event.getNewCurrent() > 0;
        }

        boolean willChange = immutableBlockState.get(this.openProperty) != hasSignal;
        if (hasSignal && willChange) {
            Object abovePos = LocationUtils.above(blockPos);
            Object aboveBlockState = FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, abovePos);
            if (CoreReflections.clazz$RedStoneWireBlock.isInstance(FastNMS.INSTANCE.method$BlockState$getBlock(aboveBlockState))) {
                FastNMS.INSTANCE.method$LevelWriter$setBlock(level, abovePos, MBlocks.AIR$defaultState, UpdateOption.UPDATE_ALL.flags());
                World world = new BukkitWorld(FastNMS.INSTANCE.method$Level$getCraftWorld(level));
                world.dropItemNaturally(
                        new Vec3d(FastNMS.INSTANCE.field$Vec3i$x(abovePos) + 0.5, FastNMS.INSTANCE.field$Vec3i$y(abovePos) + 0.5, FastNMS.INSTANCE.field$Vec3i$z(abovePos) + 0.5),
                        BukkitItemManager.instance().createWrappedItem(ItemKeys.REDSTONE, null)
                );
                if (FastNMS.INSTANCE.method$BlockGetter$getBlockState(level, blockPos) != blockPos) {
                    return;
                }
            }
        }

        if (willChange) {
            immutableBlockState = immutableBlockState.with(this.openProperty, hasSignal);
            // todo 播放声音
            FastNMS.INSTANCE.method$Level$getCraftWorld(level).sendGameEvent(null,
                    immutableBlockState.get(this.openProperty) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE,
                    new Vector(FastNMS.INSTANCE.field$Vec3i$x(blockPos), FastNMS.INSTANCE.field$Vec3i$y(blockPos), FastNMS.INSTANCE.field$Vec3i$z(blockPos))
            );
        }

        FastNMS.INSTANCE.method$LevelWriter$setBlock(level, blockPos, immutableBlockState.customBlockState().handle(), UpdateOption.Flags.UPDATE_CLIENTS);
        if (this.waterloggedProperty != null && immutableBlockState.get(this.waterloggedProperty)) {
            FastNMS.INSTANCE.method$LevelAccessor$scheduleFluidTick(level, blockPos, MFluids.WATER, 5);
        }
    }

    private void toggle(ImmutableBlockState state, World world, BlockPos pos, @Nullable Player player) {
        ImmutableBlockState newState = state.cycle(this.openProperty);
        FastNMS.INSTANCE.method$LevelWriter$setBlock(world.serverWorld(), LocationUtils.toBlockPos(pos), newState.customBlockState().handle(), UpdateOption.Flags.UPDATE_CLIENTS);
        // todo 播放声音，需要补一套交互的声音系统
        ((org.bukkit.World) world.platformWorld()).sendGameEvent(
                player != null ? (org.bukkit.entity.Player) player.platformPlayer() : null,
                newState.get(this.openProperty) ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE,
                new Vector(pos.x(), pos.y(), pos.z())
        );
    }

    @SuppressWarnings("unchecked")
    public static class Factory implements BlockBehaviorFactory {
        @Override
        public BlockBehavior create(CustomBlock block, Map<String, Object> arguments) {
            Property<Boolean> waterlogged = (Property<Boolean>) block.getProperty("waterlogged");
            Property<SingleBlockHalf> half = (Property<SingleBlockHalf>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("half"), "warning.config.block.behavior.trapdoor.missing_half");
            Property<HorizontalDirection> facing = (Property<HorizontalDirection>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("facing"), "warning.config.block.behavior.trapdoor.missing_facing");
            Property<Boolean> open = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("open"), "warning.config.block.behavior.trapdoor.missing_open");
            Property<Boolean> powered = (Property<Boolean>) ResourceConfigUtils.requireNonNullOrThrow(block.getProperty("powered"), "warning.config.block.behavior.trapdoor.missing_powered");
            boolean canOpenWithHand = (boolean) arguments.getOrDefault("can-open-with-hand", true);
            boolean canOpenByWindCharge = (boolean) arguments.getOrDefault("can-open-by-wind-charge", true);
            return new TrapDoorBlockBehavior(block, waterlogged, half, facing, powered, open, canOpenWithHand, canOpenByWindCharge);
        }
    }
}
