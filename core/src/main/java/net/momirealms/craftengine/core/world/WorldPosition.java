package net.momirealms.craftengine.core.world;

public class WorldPosition {
    private final World world;
    private final double x;
    private final double y;
    private final double z;
    private final float xRot;
    private final float yRot;

    public WorldPosition(World world, Position position, float xRot, float yRot) {
        this.x = position.x();
        this.y = position.y();
        this.z = position.z();
        this.world = world;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public WorldPosition(World world, double x, double y, double z, float xRot, float yRot) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.xRot = xRot;
        this.yRot = yRot;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public World world() {
        return world;
    }

    public float xRot() {
        return xRot;
    }

    public float yRot() {
        return yRot;
    }
}
