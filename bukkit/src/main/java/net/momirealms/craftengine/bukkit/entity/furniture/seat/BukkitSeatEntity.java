package net.momirealms.craftengine.bukkit.entity.furniture.seat;

import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.entity.furniture.BukkitFurniture;
import net.momirealms.craftengine.core.entity.furniture.Furniture;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.entity.seat.SeatEntity;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import org.bukkit.entity.Entity;
import org.joml.Vector3f;

public abstract class BukkitSeatEntity extends BukkitEntity implements SeatEntity {
    private final BukkitFurniture furniture;
    private final Vector3f vector3f;
    private final int playerID;
    private boolean destroyed = false;

    public BukkitSeatEntity(Entity entity, Furniture furniture, Vector3f vector3f, int playerID) {
        super(entity);
        this.furniture = (BukkitFurniture) furniture;
        this.vector3f = vector3f;
        this.playerID = playerID;
    }

    @Override
    public void add(NetWorkUser to) {}

    @Override
    public void dismount(Player player) {
        if (player == null) return;
        player.setSeat(null);
        onDismount(player);
        destroy();
    }

    @Override
    public void onDismount(Player player) {}

    @Override
    public void event(Player player, Object event) {}

    @Override
    public void destroy() {
        if (destroyed) return;
        destroyed = true;

        org.bukkit.entity.Entity entity = this.literalObject();
        if (entity == null) return;

        for (org.bukkit.entity.Entity passenger : entity.getPassengers()) {
            entity.removePassenger(passenger);
            if (passenger instanceof org.bukkit.entity.Player p && p.getEntityId() == this.playerID) {
                Player cePlayer = BukkitAdaptors.adapt(p);
                dismount(cePlayer);
            }
        }
        entity.remove();
        furniture.removeSeatEntity(playerID());
        furniture.removeOccupiedSeat(vector3f());
    }

    public boolean destroyed() {
        return destroyed;
    }

    @Override
    public BukkitFurniture furniture() {
        return this.furniture;
    }

    @Override
    public Vector3f vector3f() {
        return this.vector3f;
    }

    @Override
    public int playerID() {
        return this.playerID;
    }
}
