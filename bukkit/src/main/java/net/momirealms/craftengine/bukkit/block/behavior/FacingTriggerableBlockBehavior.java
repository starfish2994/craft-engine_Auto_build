package net.momirealms.craftengine.bukkit.block.behavior;

import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.properties.Property;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public abstract class FacingTriggerableBlockBehavior extends BukkitBlockBehavior {
    protected static final List<Key> DEFAULT_BLACKLIST_BLOCKS = List.of(
            Key.of("minecraft:bedrock"),
            Key.of("minecraft:end_portal_frame"),
            Key.of("minecraft:end_portal"),
            Key.of("minecraft:nether_portal"),
            Key.of("minecraft:barrier"),
            Key.of("minecraft:command_block"),
            Key.of("minecraft:chain_command_block"),
            Key.of("minecraft:repeating_command_block"),
            Key.of("minecraft:structure_block"),
            Key.of("minecraft:end_gateway"),
            Key.of("minecraft:jigsaw"),
            Key.of("minecraft:structure_void"),
            Key.of("minecraft:test_instance_block"),
            Key.of("minecraft:moving_piston"),
            Key.of("minecraft:test_block"),
            Key.of("minecraft:light")
    );
    protected final Property<Direction> facingProperty;
    protected final Property<Boolean> triggeredProperty;
    protected final List<Key> blocks;
    protected final boolean whitelistMode;

    public FacingTriggerableBlockBehavior(CustomBlock customBlock, Property<Direction> facing, Property<Boolean> triggered, List<Key> blocks, boolean whitelistMode) {
        super(customBlock);
        this.facingProperty = facing;
        this.triggeredProperty = triggered;
        this.blocks = blocks;
        this.whitelistMode = whitelistMode;
    }

    @Override
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) {
        Object state = args[0];
        Object level = args[1];
        Object pos = args[2];
        boolean hasNeighborSignal = FastNMS.INSTANCE.method$SignalGetter$hasNeighborSignal(level, pos);
        ImmutableBlockState blockState = BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(state));
        if (blockState == null || blockState.isEmpty()) return;
        boolean triggeredValue = blockState.get(this.triggeredProperty);
        if (hasNeighborSignal && !triggeredValue) {
            // FastNMS.INSTANCE.method$LevelAccessor$scheduleBlockTick(level, pos, thisBlock, 1, this.getTickPriority()); // 鬼知道为什么这个无法触发 tick
            tick(state, level, pos);
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, blockState.with(this.triggeredProperty, true).customBlockState().handle(), 2);
        } else if (!hasNeighborSignal && triggeredValue) {
            FastNMS.INSTANCE.method$LevelWriter$setBlock(level, pos, blockState.with(this.triggeredProperty, false).customBlockState().handle(), 2);
        }
    }

    @Override
    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        Player player = context.getPlayer();
        Direction direction = player.getDirection().opposite();
        float yRot = player.yRot();
        if (yRot > 45 && yRot < 90) direction = Direction.UP;
        if (yRot < -45 && yRot > -90) direction = Direction.DOWN;
        return state.owner().value().defaultState().with(this.facingProperty, direction);
    }

    protected boolean blockCheck(Object blockState) {
        if (blockState == null || FastNMS.INSTANCE.method$BlockStateBase$isAir(blockState)) {
            return false;
        }
        Key blockId = Optional.ofNullable(BukkitBlockManager.instance().getImmutableBlockState(BlockStateUtils.blockStateToId(blockState)))
                .filter(state -> !state.isEmpty())
                .map(state -> state.owner().value().id())
                .orElseGet(() -> BlockStateUtils.getBlockOwnerIdFromState(blockState));
        return this.blocks.contains(blockId) == this.whitelistMode;
    }

    protected abstract Object getTickPriority();

    protected abstract void tick(Object state, Object level, Object pos);
}
