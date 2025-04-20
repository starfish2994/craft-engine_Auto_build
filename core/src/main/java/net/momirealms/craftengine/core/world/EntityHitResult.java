package net.momirealms.craftengine.core.world;

import net.momirealms.craftengine.core.util.Direction;

public class EntityHitResult {
    private final Direction direction;
    private final Vec3d position;

    public EntityHitResult(Direction direction, Vec3d position) {
        this.direction = direction;
        this.position = position;
    }

    public Direction direction() {
        return direction;
    }

    public Vec3d position() {
        return position;
    }
}
