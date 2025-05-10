package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.world.Vec3d;

public abstract class AbstractEntity implements Entity {

    @Override
    public Vec3d position() {
        return new Vec3d(x(), y(), z());
    }
}
