package net.momirealms.craftengine.core.entity.furniture;

public abstract class AbstractHitBox implements HitBox {
    protected final Seat[] seats;

    public AbstractHitBox(Seat[] seats) {
        this.seats = seats;
    }

    @Override
    public Seat[] seats() {
        return seats;
    }

}
