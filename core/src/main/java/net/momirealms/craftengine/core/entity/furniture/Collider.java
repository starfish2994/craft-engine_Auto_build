package net.momirealms.craftengine.core.entity.furniture;

import org.joml.Vector3d;
import org.joml.Vector3f;

public class Collider {
    private final Vector3d point1;
    private final Vector3d point2;
    private final boolean canBeHitByProjectile;

    public Collider(boolean canBeHitByProjectile, Vector3d point1, Vector3d point2) {
        this.canBeHitByProjectile = canBeHitByProjectile;
        this.point1 = point1;
        this.point2 = point2;
    }

    public Collider(boolean canBeHitByProjectile, Vector3f position, double width, double height) {
        this.canBeHitByProjectile = canBeHitByProjectile;
        this.point1 = new Vector3d(position.x - width / 2, position.y, position.z - width / 2);
        this.point2 = new Vector3d(position.x + width / 2, position.y + height, position.z + width / 2);
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
