package net.momirealms.craftengine.core.entity.furniture;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class Collider {
    private final Vector3f position;
    private final Vector3d point1;
    private final Vector3d point2;
    private final boolean canBeHitByProjectile;

    public Collider(boolean canBeHitByProjectile, Vector3f position, Vector3d point1, Vector3d point2) {
        this.canBeHitByProjectile = canBeHitByProjectile;
        this.position = position;
        this.point1 = point1;
        this.point2 = point2;
    }

    public Vector3f position() {
        return position;
    }

    public boolean canBeHitByProjectile() {
        return canBeHitByProjectile;
    }

    public Vector3d point1() {
        return point1;
    }

    public Vector3d point2() {
        return point2;
    }
}
