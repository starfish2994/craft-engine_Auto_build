package net.momirealms.craftengine.core.entity.furniture;

import org.joml.Vector3f;

public abstract class AbstractHitBox implements HitBox {
    protected final Seat[] seats;
    protected final Vector3f position;
    protected final boolean canUseItemOn;

    public AbstractHitBox(Seat[] seats, Vector3f position, boolean canUseItemOn) {
        this.seats = seats;
        this.position = position;
        this.canUseItemOn = canUseItemOn;
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
    public boolean canPlaceAgainst() {
        return this.canUseItemOn;
    }
}
