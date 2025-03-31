package net.momirealms.craftengine.core.entity.furniture;

import org.joml.Vector3f;

public class Collider {
    private final Vector3f point1;
    private final Vector3f point2;
    private final boolean canBeHitByProjectile;

    public Collider(boolean canBeHitByProjectile, Vector3f point1, Vector3f point2) {
        this.canBeHitByProjectile = canBeHitByProjectile;
        this.point1 = point1;
        this.point2 = point2;
    }

    public Collider(boolean canBeHitByProjectile, Vector3f position, float width, float height) {
        this.canBeHitByProjectile = canBeHitByProjectile;
        this.point1 = new Vector3f(position.x - width / 2, position.y, position.z - width / 2);
        this.point2 = new Vector3f(position.x + width / 2, position.y + height, position.z + width / 2);
    }

    public boolean canBeHitByProjectile() {
        return canBeHitByProjectile;
    }

    public Vector3f point1() {
        return point1;
    }

    public Vector3f point2() {
        return point2;
    }
}
