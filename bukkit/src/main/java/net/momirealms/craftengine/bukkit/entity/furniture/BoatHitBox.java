package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.joml.Vector3f;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BoatHitBox extends AbstractHitBox {
    public static final Factory FACTORY = new Factory();

    public BoatHitBox(Seat[] seats, Vector3f position) {
        super(seats, position);
    }

    @Override
    public Key type() {
        return HitBoxTypes.BOAT;
    }

    @Override
    public void addSpawnPackets(int[] entityId, double x, double y, double z, float yaw, Consumer<Object> packets) {
        // 生成无重力船
    }

    @Override
    public int[] acquireEntityIds(Supplier<Integer> entityIdSupplier) {
        return new int[] {entityIdSupplier.get()};
    }

    public static class Factory implements HitBoxFactory {

        @Override
        public HitBox create(Map<String, Object> arguments) {
            return new BoatHitBox(
                    HitBoxFactory.getSeats(arguments),
                    MiscUtils.getVector3f(arguments.getOrDefault("position", "0"))
            );
        }
    }
}
