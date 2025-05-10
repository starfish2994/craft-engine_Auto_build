package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.entity.Billboard;
import net.momirealms.craftengine.core.entity.ItemDisplayContext;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public interface FurnitureElement {
    Quaternionf rotation();

    Key item();

    Billboard billboard();

    ItemDisplayContext transform();

    Vector3f scale();

    Vector3f translation();

    Vector3f position();

    void initPackets(int entityId, World world, double x, double y, double z, float yaw, Quaternionf conjugated, Consumer<Object> packets);
}
