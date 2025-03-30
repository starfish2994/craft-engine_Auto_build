package net.momirealms.craftengine.core.entity.furniture;

import org.joml.Vector3f;

public abstract class AbstractHitBox implements HitBox {
    protected final Seat[] seats;
    protected final Vector3f position;

    public AbstractHitBox(Seat[] seats, Vector3f position) {
        this.seats = seats;
        this.position = position;
    }

    @Override
    public Seat[] seats() {
        return seats;
    }

    @Override
    public Vector3f position() {
        return position;
    }
}
