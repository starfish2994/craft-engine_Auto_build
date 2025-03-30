package net.momirealms.craftengine.core.entity.furniture;

import net.momirealms.craftengine.core.util.Key;

import java.util.function.Consumer;

public interface HitBox {

    Key type();

    void addSpawnPackets(int entityId, double x, double y, double z, float yaw, Consumer<Object> packets);

    Seat[] seats();
}
