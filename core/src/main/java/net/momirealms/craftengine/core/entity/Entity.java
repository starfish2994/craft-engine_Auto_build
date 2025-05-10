package net.momirealms.craftengine.core.entity;

import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;

public interface Entity {
    Key type();

    double x();

    double y();

    double z();

    Vec3d position();

    void tick();

    float getXRot();

    int entityID();

    float getYRot();

    World world();

    Direction getDirection();

    Object literalObject();
}
