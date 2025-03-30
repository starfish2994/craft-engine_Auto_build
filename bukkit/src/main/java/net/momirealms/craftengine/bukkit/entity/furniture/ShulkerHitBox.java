package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.util.Direction;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import org.joml.Vector3f;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShulkerHitBox extends AbstractHitBox {
    public static final Factory FACTORY = new Factory();
    // 1.20.6+
    private final double scale;
    private final byte peek;
    // todo或许还能做个方向，但是会麻烦点，和 yaw 有关
    private final Direction direction;

    public ShulkerHitBox(Seat[] seats, Vector3f position, double scale, byte peek, Direction direction) {
        super(seats, position);
        this.scale = scale;
        this.peek = peek;
        this.direction = direction;
    }

    public Direction direction() {
        return direction;
    }

    public byte peek() {
        return peek;
    }

    public double scale() {
        return scale;
    }

    @Override
    public Key type() {
        return HitBoxTypes.SHULKER;
    }

    @Override
    public void addSpawnPackets(int[] entityIds, double x, double y, double z, float yaw, Consumer<Object> packets) {
        // 1.生成假的展示实体
        // 2.生成假的潜影贝
        // 3.潜影贝骑展示实体
        // 4.潜影贝数据设置（隐身，尺寸，peek，方向）
    }

    @Override
    public int[] acquireEntityIds(Supplier<Integer> entityIdSupplier) {
                         // 展示实体              // 潜影贝
        return new int[] {entityIdSupplier.get(), entityIdSupplier.get()};
    }

    public static class Factory implements HitBoxFactory {

        @Override
        public HitBox create(Map<String, Object> arguments) {
            Vector3f position = MiscUtils.getVector3f(arguments.getOrDefault("position", "0"));
            double scale = MiscUtils.getAsDouble(arguments.getOrDefault("scale", "1"));
            byte peek = (byte) MiscUtils.getAsInt(arguments.getOrDefault("peek", 0));
            Direction directionEnum = Optional.ofNullable(arguments.get("direction")).map(it -> Direction.valueOf(it.toString().toUpperCase(Locale.ENGLISH))).orElse(Direction.UP);
            return new ShulkerHitBox(
                    HitBoxFactory.getSeats(arguments),
                    position,
                    scale, peek, directionEnum
            );
        }
    }
}
