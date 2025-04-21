package net.momirealms.craftengine.core.entity.furniture;

import org.joml.Vector3f;

public abstract class AbstractHitBox implements HitBox {
    protected final Seat[] seats;
    protected final Vector3f position;
    protected final boolean canUseItemOn;
    protected final boolean blocksBuilding;
    protected final boolean canBeHitByProjectile;

    public AbstractHitBox(Seat[] seats, Vector3f position, boolean canUseItemOn, boolean blocksBuilding, boolean canBeHitByProjectile) {
        this.seats = seats;
        this.position = position;
        this.canUseItemOn = canUseItemOn;
        this.blocksBuilding = blocksBuilding;
        this.canBeHitByProjectile = canBeHitByProjectile;
    }

    @Override
    public Seat[] seats() {
        return this.seats;
    }

    @Override
    public Vector3f position() {
        return this.position;
    }

    @Override
    public boolean blocksBuilding() {
        return blocksBuilding;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return canBeHitByProjectile;
    }

    @Override
    public boolean canUseItemOn() {
        return canUseItemOn;
    }
}
