package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.ShulkerData;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.util.*;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShulkerHitBox extends AbstractHitBox {
    public static final Factory FACTORY = new Factory();
    // 1.20.6+
    private final double scale;
    private final byte peek;
    // todo或许还能做个方向，但是会麻烦点，和 yaw 有关
    private final Direction direction;
    private List<Object> cachedValues;

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
        Vector3f offset = QuaternionUtils.toQuaternionf(0f, Math.toRadians(180f - yaw), 0f).conjugate().transform(new Vector3f(position()));
        try {
            packets.accept(Reflections.constructor$ClientboundAddEntityPacket.newInstance(
                    entityIds[0], UUID.randomUUID(), x + offset.x, y + offset.y, z - offset.z, 0, yaw,
                    Reflections.instance$EntityType$ITEM_DISPLAY, 0, Reflections.instance$Vec3$Zero, 0
            ));
            packets.accept(Reflections.constructor$ClientboundAddEntityPacket.newInstance(
                    entityIds[1], UUID.randomUUID(), x + offset.x, y + offset.y, z - offset.z, 0, yaw,
                    Reflections.instance$EntityType$SHULKER, 0, Reflections.instance$Vec3$Zero, 0
            ));
            packets.accept(Reflections.constructor$ClientboundSetEntityDataPacket.newInstance(entityIds[1], getCachedValues()));
            packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetPassengersPacket(entityIds[0], entityIds[1]));
            if (VersionHelper.isVersionNewerThan1_20_5()) {
                Object attributeInstance = Reflections.constructor$AttributeInstance.newInstance(Reflections.instance$Holder$Attribute$scale, (Consumer<?>) (o) -> {});
                Reflections.method$AttributeInstance$setBaseValue.invoke(attributeInstance, scale);
                packets.accept(Reflections.constructor$ClientboundUpdateAttributesPacket0.newInstance(entityIds[1], Collections.singletonList(attributeInstance)));
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to construct shulker hitbox spawn packet", e);
        }
    }

    private synchronized List<Object> getCachedValues() {
        if (this.cachedValues == null) {
            this.cachedValues = new ArrayList<>();
            ShulkerData.Peek.addEntityDataIfNotDefaultValue(peek, this.cachedValues);
            ShulkerData.AttachFace.addEntityDataIfNotDefaultValue(DirectionUtils.toNMSDirection(direction), this.cachedValues);
            ShulkerData.NoGravity.addEntityDataIfNotDefaultValue(true, this.cachedValues);
            ShulkerData.Silent.addEntityDataIfNotDefaultValue(true, this.cachedValues);
            ShulkerData.MobFlags.addEntityDataIfNotDefaultValue((byte) 0x01, this.cachedValues); // 无ai
            ShulkerData.SharedFlags.addEntityDataIfNotDefaultValue((byte) 0x20, this.cachedValues); // 不可见
        }
        return this.cachedValues;
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
