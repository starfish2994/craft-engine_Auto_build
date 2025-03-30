package net.momirealms.craftengine.core.entity.furniture;

import org.joml.Vector3f;

public class Collider {
    private final Vector3f position;
    private final double width;
    private final double height;
    private final boolean canBeHitByProjectile;

    public Collider(boolean canBeHitByProjectile, double height, Vector3f position, double width) {
        this.canBeHitByProjectile = canBeHitByProjectile;
        this.height = height;
        this.position = position;
        this.width = width;
    }

    public boolean canBeHitByProjectile() {
        return canBeHitByProjectile;
    }

    public double height() {
        return height;
    }

    public Vector3f position() {
        return position;
    }

    public double width() {
        return width;
    }
}
