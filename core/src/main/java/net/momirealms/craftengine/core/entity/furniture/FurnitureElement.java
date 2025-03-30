package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.util.Key;
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

    void addSpawnPackets(int entityId, double x, double y, double z, float yaw, Consumer<Object> packets);
}
