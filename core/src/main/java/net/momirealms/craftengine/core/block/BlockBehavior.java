package net.momirealms.craftengine.core.block;

import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;

import java.util.Optional;
import java.util.concurrent.Callable;

public abstract class BlockBehavior {

    @SuppressWarnings("unchecked")
    public <T extends BlockBehavior> Optional<T> getAs(Class<T> tClass) {
        if (tClass.isInstance(this)) {
            return Optional.of((T) this);
        }
        return Optional.empty();
    }

    // BlockState state, Rotation rotation
    public Object rotate(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return superMethod.call();
    }

    // BlockState state, Mirror mirror
    public Object mirror(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return superMethod.call();
    }

    // 1.20.1-1.21.1 Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos
    // 1.21.2+ LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random
    public Object updateShape(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return args[0];
    }

    // BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston
    public void neighborChanged(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    // ServerLevel level, BlockPos pos, RandomSource random
    public void tick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    // ServerLevel level, BlockPos pos, RandomSource random
    public void randomTick(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    // 1.20-1.20.4 BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify, UseOnContext context
    // 1.20.5+ Level level, BlockPos pos, BlockState oldState, boolean movedByPiston
    public void onPlace(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        superMethod.call();
    }

    // 1.20+ BlockState state, LevelReader world, BlockPos pos
    public boolean canSurvive(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return (boolean) superMethod.call();
    }

    // 1.20-1.20.4 BlockState state, BlockGetter world, BlockPos pos, PathComputationType type
    // 1.20.5+ BlockState state, PathComputationType pathComputationType
    public boolean isPathFindable(Object thisBlock, Object[] args, Callable<Object> superMethod) throws Exception {
        return (boolean) superMethod.call();
    }

    // Level level, BlockPos pos, FallingBlockEntity fallingBlock
    public void onBrokenAfterFall(Object thisBlock, Object[] args) throws Exception {
    }

    // Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock
    public void onLand(Object thisBlock, Object[] args) throws Exception {
    }

    // LevelReader level, BlockPos pos, BlockState state
    public boolean isValidBoneMealTarget(Object thisBlock, Object[] args) throws Exception {
        return false;
    }

    // Level level, RandomSource random, BlockPos pos, BlockState state
    public boolean isBoneMealSuccess(Object thisBlock, Object[] args) throws Exception {
        return false;
    }

    // ServerLevel level, RandomSource random, BlockPos pos, BlockState state
    public void performBoneMeal(Object thisBlock, Object[] args) throws Exception {
    }

    // 1.21+ BlockState state, ServerLevel level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer
    public void onExplosionHit(Object thisBlock, Object[] args, Callable<Object> superMethod) {
    }

    public ImmutableBlockState updateStateForPlacement(BlockPlaceContext context, ImmutableBlockState state) {
        return state;
    }

    public void setPlacedBy(BlockPlaceContext context, ImmutableBlockState state) {
    }

    public InteractionResult useOnBlock(UseOnContext context, ImmutableBlockState state) {
        return InteractionResult.PASS;
    }

    public Object pickupBlock(Object thisObj, Object[] args, Callable<Object> superMethod) throws Exception {
        return superMethod.call();
    }

    public boolean placeLiquid(Object thisObj, Object[] args, Callable<Object> superMethod) {
        return false;
    }

    public boolean canPlaceLiquid(Object thisObj, Object[] args, Callable<Object> superMethod) {
        return false;
    }
}
