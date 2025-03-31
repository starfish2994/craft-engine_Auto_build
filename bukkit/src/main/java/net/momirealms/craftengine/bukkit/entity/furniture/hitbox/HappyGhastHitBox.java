package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HappyGhastHitBox extends AbstractHitBox {
    public static final Factory FACTORY = new Factory();
    private final double scale;

    public HappyGhastHitBox(Seat[] seats, Vector3f position, double scale) {
        super(seats, position);
        this.scale = scale;
    }

    @Override
    public Key type() {
        return HitBoxTypes.HAPPY_GHAST;
    }

    public double scale() {
        return scale;
    }

    @Override
    public void addSpawnPackets(int[] entityId, double x, double y, double z, float yaw, Quaternionf conjugated, BiConsumer<Object, Boolean> packets) {
        // todo 乐魂
    }

    @Override
    public int[] acquireEntityIds(Supplier<Integer> entityIdSupplier) {
        return new int[] {entityIdSupplier.get()};
    }

    public static class Factory implements HitBoxFactory {

        @Override
        public HitBox create(Map<String, Object> arguments) {
            double scale = MiscUtils.getAsDouble(arguments.getOrDefault("scale", "1"));
            return new HappyGhastHitBox(
                    HitBoxFactory.getSeats(arguments),
                    MiscUtils.getVector3f(arguments.getOrDefault("position", "0")),
                    scale
            );
        }
    }
}
