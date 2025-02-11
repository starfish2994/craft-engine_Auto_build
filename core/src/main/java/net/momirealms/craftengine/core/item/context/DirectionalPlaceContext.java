package net.momirealms.craftengine.core.item.context;

import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.world.BlockHitResult;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;

public class DirectionalPlaceContext extends BlockPlaceContext {
    private final Direction direction;

    public DirectionalPlaceContext(World world, BlockPos pos, Direction facing, Item<?> stack, Direction side) {
        super(world, null, InteractionHand.MAIN_HAND, stack, new BlockHitResult(Vec3d.atBottomCenterOf(pos), side, pos, false));
        this.direction = facing;
    }

    @Override
    public BlockPos getClickedPos() {
        return this.getHitResult().getBlockPos();
    }

    @Override
    public boolean canPlace() {
        return this.getLevel().getBlockAt(this.getHitResult().getBlockPos()).canBeReplaced(this);
    }

    @Override
    public boolean replacingClickedOnBlock() {
        return this.canPlace();
    }

    public Direction getNearestLookingDirection() {
        return Direction.DOWN;
    }

    public Direction[] getNearestLookingDirections() {
        return switch (this.direction) {
            case UP ->
                    new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
            case NORTH ->
                    new Direction[]{Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.SOUTH};
            case SOUTH ->
                    new Direction[]{Direction.DOWN, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.NORTH};
            case WEST ->
                    new Direction[]{Direction.DOWN, Direction.WEST, Direction.SOUTH, Direction.UP, Direction.NORTH, Direction.EAST};
            case EAST ->
                    new Direction[]{Direction.DOWN, Direction.EAST, Direction.SOUTH, Direction.UP, Direction.NORTH, Direction.WEST};
            default ->
                    new Direction[]{Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP};
        };
    }

    @Override
    public Direction getHorizontalDirection() {
        return this.direction.axis() == Direction.Axis.Y ? Direction.NORTH : this.direction;
    }

    @Override
    public boolean isSecondaryUseActive() {
        return false;
    }

    @Override
    public float getRotation() {
        return (float) (this.direction.data2d() * 90);
    }
}
