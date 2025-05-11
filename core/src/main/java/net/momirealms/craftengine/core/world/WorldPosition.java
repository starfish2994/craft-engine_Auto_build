package net.momirealms.craftengine.core.world;

public class WorldPosition {
    private final World world;
    private final Position position;

    public WorldPosition(Position position, World world) {
        this.position = position;
        this.world = world;
    }

    public Position position() {
        return position;
    }

    public World world() {
        return world;
    }
}
