package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.InteractionEntityData;
import net.momirealms.craftengine.bukkit.entity.data.ShulkerData;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.util.*;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShulkerHitBox extends AbstractHitBox {
    public static final Factory FACTORY = new Factory();
    // 1.20.6+
    private final float scale;
    private final byte peek;
    private final boolean interactive;
    private final boolean interactionEntity;
    // todo或许还能做个方向，但是会麻烦点，和 yaw 有关
    private final Direction direction = Direction.UP;
    private final List<Object> cachedShulkerValues = new ArrayList<>();
    private final List<Object> cachedInteractionValues = new ArrayList<>();

    public ShulkerHitBox(Seat[] seats, Vector3f position, float scale, byte peek, boolean interactionEntity, boolean interactive) {
        super(seats, position);
        this.scale = scale;
        this.peek = peek;
        this.interactive = interactive;
        this.interactionEntity = interactionEntity;

        ShulkerData.Peek.addEntityDataIfNotDefaultValue(peek, this.cachedShulkerValues);
        ShulkerData.Color.addEntityDataIfNotDefaultValue((byte) 0, this.cachedShulkerValues);
//      ShulkerData.AttachFace.addEntityDataIfNotDefaultValue(DirectionUtils.toNMSDirection(direction), this.cachedShulkerValues);
        ShulkerData.NoGravity.addEntityDataIfNotDefaultValue(true, this.cachedShulkerValues);
        ShulkerData.Silent.addEntityDataIfNotDefaultValue(true, this.cachedShulkerValues);
        ShulkerData.MobFlags.addEntityDataIfNotDefaultValue((byte) 0x01, this.cachedShulkerValues); // 无ai
        ShulkerData.SharedFlags.addEntityDataIfNotDefaultValue((byte) 0x20, this.cachedShulkerValues); // 不可见

        if (this.interactionEntity) {
            // make it a litter bigger
            InteractionEntityData.Height.addEntityDataIfNotDefaultValue(getPhysicalPeek(peek) * scale + 0.01f, cachedInteractionValues);
            InteractionEntityData.Width.addEntityDataIfNotDefaultValue(scale + 0.01f, cachedInteractionValues);
            InteractionEntityData.Responsive.addEntityDataIfNotDefaultValue(interactive, cachedInteractionValues);
        }
    }

    @Override
    public Optional<Collider> optionalCollider() {
        float peek = getPhysicalPeek(this.peek() * 0.01F);
        double x1 = -scale * 0.5;
        double y1 = 0.0;
        double z1 = -scale * 0.5;
        double x2 = scale * 0.5;
        double y2 = scale;
        double z2 = scale * 0.5;

        double dx = (double) direction.stepX() * peek * (double) scale;
        if (dx > 0) {
            x2 += dx;
        } else if (dx < 0) {
            x1 += dx;
        }
        double dy = (double) direction.stepY() * peek * (double) scale;
        if (dy > 0) {
            y2 += dy;
        } else if (dy < 0) {
            y1 += dy;
        }
        double dz = (double) direction.stepZ() * peek * (double) scale;
        if (dz > 0) {
            z2 += dz;
        } else if (dz < 0) {
            z1 += dz;
        }
        return Optional.of(new Collider(
                true,
                new Vector3d(x1, y1, z1),
                new Vector3d(x2, y2, z2)
        ));
    }

    private static float getPhysicalPeek(float peek) {
        return 0.5F - MCUtils.sin((0.5F + peek) * 3.1415927F) * 0.5F;
    }

    public boolean interactionEntity() {
        return interactionEntity;
    }

    public boolean interactive() {
        return interactive;
    }

    public Direction direction() {
        return direction;
    }

    public byte peek() {
        return peek;
    }

    public float scale() {
        return scale;
    }

    @Override
    public Key type() {
        return HitBoxTypes.SHULKER;
    }

    @Override
    public void addSpawnPackets(int[] entityIds, double x, double y, double z, float yaw, Quaternionf conjugated, BiConsumer<Object, Boolean> packets) {
        Vector3f offset = conjugated.transform(new Vector3f(position()));
        try {
            packets.accept(Reflections.constructor$ClientboundAddEntityPacket.newInstance(
                    entityIds[0], UUID.randomUUID(), x + offset.x, y + offset.y, z - offset.z, 0, yaw,
                    Reflections.instance$EntityType$ITEM_DISPLAY, 0, Reflections.instance$Vec3$Zero, 0
            ), false);
            packets.accept(Reflections.constructor$ClientboundAddEntityPacket.newInstance(
                    entityIds[1], UUID.randomUUID(), x + offset.x, y + offset.y, z - offset.z, 0, yaw,
                    Reflections.instance$EntityType$SHULKER, 0, Reflections.instance$Vec3$Zero, 0
            ), false);
            packets.accept(Reflections.constructor$ClientboundSetEntityDataPacket.newInstance(entityIds[1], List.copyOf(this.cachedShulkerValues)), false);
            packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetPassengersPacket(entityIds[0], entityIds[1]), false);
            if (VersionHelper.isVersionNewerThan1_20_5()) {
                Object attributeInstance = Reflections.constructor$AttributeInstance.newInstance(Reflections.instance$Holder$Attribute$scale, (Consumer<?>) (o) -> {});
                Reflections.method$AttributeInstance$setBaseValue.invoke(attributeInstance, scale);
                packets.accept(Reflections.constructor$ClientboundUpdateAttributesPacket0.newInstance(entityIds[1], Collections.singletonList(attributeInstance)), false);
            }
            if (this.interactionEntity) {
                // make it a litter lower
                packets.accept(Reflections.constructor$ClientboundAddEntityPacket.newInstance(
                        entityIds[2], UUID.randomUUID(), x + offset.x, y + offset.y - 0.005f, z - offset.z, 0, yaw,
                        Reflections.instance$EntityType$INTERACTION, 0, Reflections.instance$Vec3$Zero, 0
                ), true);
                packets.accept(Reflections.constructor$ClientboundSetEntityDataPacket.newInstance(entityIds[2], List.copyOf(this.cachedInteractionValues)), true);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to construct shulker hitbox spawn packet", e);
        }
    }

    @Override
    public int[] acquireEntityIds(Supplier<Integer> entityIdSupplier) {
        if (this.interactionEntity) {
                            // 展示实体                 // 潜影贝               // 交互实体
            return new int[] {entityIdSupplier.get(), entityIdSupplier.get(), entityIdSupplier.get()};
        } else {
                            // 展示实体                 // 潜影贝
            return new int[] {entityIdSupplier.get(), entityIdSupplier.get()};
        }
    }

    public static class Factory implements HitBoxFactory {

        @Override
        public HitBox create(Map<String, Object> arguments) {
            Vector3f position = MiscUtils.getVector3f(arguments.getOrDefault("position", "0"));
            float scale = MiscUtils.getAsFloat(arguments.getOrDefault("scale", "1"));
            byte peek = (byte) MiscUtils.getAsInt(arguments.getOrDefault("peek", 0));
            Direction directionEnum = Optional.ofNullable(arguments.get("direction")).map(it -> Direction.valueOf(it.toString().toUpperCase(Locale.ENGLISH))).orElse(Direction.UP);
            boolean interactive = (boolean) arguments.getOrDefault("interactive", true);
            boolean interactionEntity = (boolean) arguments.getOrDefault("interaction-entity", true);
            return new ShulkerHitBox(
                    HitBoxFactory.getSeats(arguments),
                    position,
                    scale, peek, interactionEntity, interactive
            );
        }
    }
}
