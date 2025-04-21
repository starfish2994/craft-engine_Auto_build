package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.core.entity.furniture.Collider;
import net.momirealms.craftengine.core.entity.furniture.ColliderType;

public class BukkitCollider implements Collider {
    private final CollisionEntity collisionEntity;
    private final ColliderType type;

    public BukkitCollider(CollisionEntity collisionEntity, ColliderType type) {
        this.collisionEntity = collisionEntity;
        this.type = type;
    }

    @Override
    public void destroy() {
        this.collisionEntity.destroy();
    }

    @Override
    public int entityId() {
        return this.collisionEntity.getId();
    }

    @Override
    public ColliderType type() {
        return this.type;
    }

    @Override
    public Object handle() {
        return this.collisionEntity;
    }
}
