package net.momirealms.craftengine.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.momirealms.craftengine.mod.CraftEnginePlugin;
import net.momirealms.craftengine.mod.util.NoteBlockUtils;
import net.momirealms.craftengine.shared.ObjectHolder;
import net.momirealms.craftengine.shared.block.*;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class CraftEngineBlock
        extends Block
        implements BehaviorHolder, ShapeHolder, NoteBlockIndicator, Fallable, BonemealableBlock
        //TODO , SimpleWaterloggedBlock
{
    private static final StoneBlockShape STONE = new StoneBlockShape(Blocks.STONE.defaultBlockState());
    private boolean isNoteBlock;
    public ObjectHolder<BlockBehavior> behaviorHolder;
    public ObjectHolder<BlockShape> shapeHolder;
    public boolean isClientSideNoteBlock;

    public CraftEngineBlock(Properties properties) {
        super(properties);
        this.behaviorHolder = new ObjectHolder<>(EmptyBlockBehavior.INSTANCE);
        this.shapeHolder = new ObjectHolder<>(STONE);
    }

    public void setNoteBlock(boolean noteBlock) {
        isNoteBlock = noteBlock;
    }

    @Override
    public ObjectHolder<BlockBehavior> getBehaviorHolder() {
        return behaviorHolder;
    }

    @Override
    public ObjectHolder<BlockShape> getShapeHolder() {
        return shapeHolder;
    }

    @Override
    public boolean isNoteBlock() {
        return isClientSideNoteBlock;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        try {
            return (VoxelShape) shapeHolder.value().getShape(this, new Object[]{state, level, pos, context});
        } catch (Exception e) {
            e.printStackTrace();
            return super.getShape(state, level, pos, context);
        }
    }

    @Override
    public @NotNull BlockState rotate(@NotNull BlockState state, @NotNull Rotation rotation) {
        try {
            return (BlockState) this.behaviorHolder.value().rotate(this, new Object[]{state, rotation}, () -> super.rotate(state, rotation));
        } catch (Exception e) {
            e.printStackTrace();
            return super.rotate(state, rotation);
        }
    }

    @Override
    public @NotNull BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
        try {
            return (BlockState) this.behaviorHolder.value().mirror(this, new Object[]{state, mirror}, () -> super.mirror(state, mirror));
        } catch (Exception e) {
            e.printStackTrace();
            return super.mirror(state, mirror);
        }
    }

    @Override
    public void tick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        try {
            this.behaviorHolder.value().tick(this, new Object[]{state, level, pos, random}, () -> {
                 super.tick(state, level, pos, random);
                 return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            super.tick(state, level, pos, random);
        }
    }

    @Override
    public void randomTick(@NotNull BlockState state, @NotNull ServerLevel level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        try {
            behaviorHolder.value().randomTick(this, new Object[]{state, level, pos, random}, () -> {
                super.randomTick(state, level, pos, random);
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            super.randomTick(state, level, pos, random);
        }
    }

    @Override
    public void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
        try {
            behaviorHolder.value().onPlace(this, new Object[]{state, level, pos, oldState, movedByPiston}, () -> {
                super.onPlace(state, level, pos, oldState, movedByPiston);
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
            super.onPlace(state, level, pos, oldState, movedByPiston);
        }
    }

    @Override
    public void onBrokenAfterFall(@NotNull Level level, @NotNull BlockPos pos, @NotNull FallingBlockEntity fallingBlock) {
        try {
            behaviorHolder.value().onBrokenAfterFall(this, new Object[]{level, pos, fallingBlock});
        } catch (Exception e) {
            e.printStackTrace();
            Fallable.super.onBrokenAfterFall(level, pos, fallingBlock);
        }
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, @NotNull BlockPos pos) {
        try {
            return behaviorHolder.value().canSurvive(this, new Object[]{state, level, pos}, () -> super.canSurvive(state, level, pos));
        } catch (Exception e) {
            e.printStackTrace();
            return super.canSurvive(state, level, pos);
        }
    }

    @Override
    public BlockState updateShape(@NotNull BlockState state,
                                  @NotNull Direction direction,
                                  @NotNull BlockState neighborState,
                                  @NotNull LevelAccessor world,
                                  @NotNull BlockPos pos,
                                  @NotNull BlockPos neighborPos) {
        try {
            if (isNoteBlock && world instanceof ServerLevel serverLevel) {
                startNoteBlockChain(direction, serverLevel, pos);
            }
            return (BlockState) behaviorHolder.value().updateShape(this, new Object[]{state, direction, neighborState, world, pos, neighborPos}, () -> super.updateShape(state, direction, neighborState, world, pos, neighborPos));
        } catch (Exception e) {
            e.printStackTrace();
            return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
        }
    }

    private static void startNoteBlockChain(Direction direction, ServerLevel serverLevel, BlockPos blockPos) {
        int id = direction.get3DDataValue();
        // Y axis
        if (id == 0 || id == 1) {
            ServerChunkCache chunkSource = serverLevel.chunkSource;
            chunkSource.blockChanged(blockPos);
            if (id == 1) {
                noteBlockChainUpdate(serverLevel, chunkSource, Direction.DOWN, blockPos, 0);
            } else {
                noteBlockChainUpdate(serverLevel, chunkSource, Direction.UP, blockPos, 0);
            }
        }
    }

    public static void noteBlockChainUpdate(ServerLevel level, ServerChunkCache chunkSource, Direction direction, BlockPos blockPos, int times) {
        if (times >= CraftEnginePlugin.maxChainUpdate()) return;
        BlockPos relativePos = blockPos.relative(direction);
        BlockState state = level.getBlockState(relativePos);
        if (NoteBlockUtils.CLIENT_SIDE_NOTE_BLOCKS.contains(state)) {
            chunkSource.blockChanged(relativePos);
            noteBlockChainUpdate(level, chunkSource, direction, relativePos, times+1);
        }
    }

    @Override
    public boolean isValidBonemealTarget(@NotNull LevelReader world, @NotNull BlockPos pos, @NotNull BlockState state, boolean isClient) {
        try {
            return behaviorHolder.value().isValidBoneMealTarget(this, new Object[]{world, pos, state, isClient});
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isBonemealSuccess(@NotNull Level level, @NotNull RandomSource randomSource, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        try {
            return behaviorHolder.value().isBoneMealSuccess(this, new Object[]{level, randomSource, blockPos, blockState});
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void performBonemeal(@NotNull ServerLevel serverLevel, @NotNull RandomSource randomSource, @NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        try {
            behaviorHolder.value().performBoneMeal(this, new Object[]{serverLevel, randomSource, blockPos, blockState});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLand(@NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull BlockState replaceableState, @NotNull FallingBlockEntity fallingBlock) {
        try {
            behaviorHolder.value().onLand(this, new Object[]{level, pos, state, replaceableState, fallingBlock});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public boolean canPlaceLiquid(@Nullable Player player, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Fluid fluid) {
//        try {
//            return behaviorHolder.value().canPlaceLiquid(this, new Object[]{player, level, pos, state, fluid}, () -> SimpleWaterloggedBlock.super.canPlaceLiquid(player, level, pos, state, fluid));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return SimpleWaterloggedBlock.super.canPlaceLiquid(player, level, pos, state, fluid);
//        }
//    }
//
//    @Override
//    public boolean placeLiquid(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull FluidState fluidState) {
//        try {
//            return behaviorHolder.value().placeLiquid(this, new Object[]{level, pos, state, fluidState}, () -> SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return SimpleWaterloggedBlock.super.placeLiquid(level, pos, state, fluidState);
//        }
//    }
//
//    @NotNull
//    @Override
//    public ItemStack pickupBlock(@Nullable Player player, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
//        try {
//            return (ItemStack) behaviorHolder.value().pickupBlock(this, new Object[]{player, level, pos, state}, () -> SimpleWaterloggedBlock.super.pickupBlock(player, level, pos, state));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return SimpleWaterloggedBlock.super.pickupBlock(player, level, pos, state);
//        }
//    }
}
