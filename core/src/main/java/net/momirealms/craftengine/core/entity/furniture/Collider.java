package net.momirealms.craftengine.core.entity.furniture;

public interface Collider {

    void destroy();

    int entityId();

    ColliderType type();

    Object handle();
}
