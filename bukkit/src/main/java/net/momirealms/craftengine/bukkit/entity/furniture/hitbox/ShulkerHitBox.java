package net.momirealms.craftengine.bukkit.entity.furniture.hitbox;

import net.momirealms.craftengine.bukkit.entity.data.InteractionEntityData;
import net.momirealms.craftengine.bukkit.entity.data.ShulkerData;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.DirectionUtils;
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
    private final Direction direction;
    private final List<Object> cachedShulkerValues = new ArrayList<>();
    private final List<Object> cachedInteractionValues = new ArrayList<>();
    private final float yOffset;

    public ShulkerHitBox(Seat[] seats, Vector3f position, Direction direction, float scale, byte peek, boolean interactionEntity, boolean interactive) {
        super(seats, position);
        this.direction = direction;
        this.scale = scale;
        this.peek = peek;
        this.interactive = interactive;
        this.interactionEntity = interactionEntity;

        ShulkerData.Peek.addEntityDataIfNotDefaultValue(peek, this.cachedShulkerValues);
        ShulkerData.Color.addEntityDataIfNotDefaultValue((byte) 0, this.cachedShulkerValues);
        ShulkerData.NoGravity.addEntityDataIfNotDefaultValue(true, this.cachedShulkerValues);
        ShulkerData.Silent.addEntityDataIfNotDefaultValue(true, this.cachedShulkerValues);
        ShulkerData.MobFlags.addEntityDataIfNotDefaultValue((byte) 0x01, this.cachedShulkerValues); // 无ai
//        ShulkerData.SharedFlags.addEntityDataIfNotDefaultValue((byte) 0x20, this.cachedShulkerValues); // 不可见

        float shulkerHeight = (getPhysicalPeek(peek * 0.01F) + 1) * scale;

        if (this.interactionEntity) {
            // make it a litter bigger
            InteractionEntityData.Height.addEntityDataIfNotDefaultValue(shulkerHeight + 0.01f, cachedInteractionValues);
            InteractionEntityData.Width.addEntityDataIfNotDefaultValue(scale + 0.005f, cachedInteractionValues);
            InteractionEntityData.Responsive.addEntityDataIfNotDefaultValue(interactive, cachedInteractionValues);
        }

        if (this.direction == Direction.DOWN) {
            this.yOffset = -shulkerHeight + 1;
        } else {
            this.yOffset = 0;
        }
    }

    public Collider createCollider(Direction d) {
        float peek = getPhysicalPeek(this.peek() * 0.01F);
        double x1 = -this.scale * 0.5;
        double y1 = 0.0;
        double z1 = -this.scale * 0.5;
        double x2 = this.scale * 0.5;
        double y2 = this.scale;
        double z2 = this.scale * 0.5;

        double dx = (double) d.stepX() * peek * (double) this.scale;
        if (dx > 0) {
            x2 += dx;
        } else if (dx < 0) {
            x1 += dx;
        }
        double dy = (double) d.stepY() * peek * (double) this.scale;
        if (dy > 0) {
            y2 += dy;
        } else if (dy < 0) {
            y1 += dy;
        }
        double dz = (double) d.stepZ() * peek * (double) this.scale;
        if (dz > 0) {
            z2 += dz;
        } else if (dz < 0) {
            z1 += dz;
        }
        return new Collider(
                true,
                this.position,
                new Vector3d(x1, y1, z1),
                new Vector3d(x2, y2, z2)
        );
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
    public void initPacketsAndColliders(int[] entityIds, double x, double y, double z, float yaw, Quaternionf conjugated, BiConsumer<Object, Boolean> packets, Consumer<Collider> collider) {
        Vector3f offset = conjugated.transform(new Vector3f(position()));
        try {
            double originalY = y + offset.y;
            double integerPart = Math.floor(originalY);
            double fractionalPart = originalY - integerPart;
            double processedY = (fractionalPart >= 0.5) ? integerPart + 1 : originalY;
            packets.accept(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                    entityIds[0], UUID.randomUUID(), x + offset.x, originalY, z - offset.z, 0, yaw,
                    Reflections.instance$EntityType$ITEM_DISPLAY, 0, Reflections.instance$Vec3$Zero, 0
            ), false);
            packets.accept(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                    entityIds[1], UUID.randomUUID(), x + offset.x, processedY, z - offset.z, 0, yaw,
                    Reflections.instance$EntityType$SHULKER, 0, Reflections.instance$Vec3$Zero, 0
            ), false);
            packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityIds[1], List.copyOf(this.cachedShulkerValues)), false);
            // add passengers
            packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetPassengersPacket(entityIds[0], entityIds[1]), false);
            // fix some special occasions
            if (originalY != processedY) {
                double deltaY = originalY - processedY;
                short ya = (short) (deltaY * 8192);
                packets.accept(Reflections.constructor$ClientboundMoveEntityPacket$Pos.newInstance(
                        entityIds[1], (short) 0, ya, (short) 0, true
                ), false);
            }
            // set shulker scale
            if (VersionHelper.isVersionNewerThan1_20_5() && this.scale != 1) {
                Object attributeInstance = Reflections.constructor$AttributeInstance.newInstance(Reflections.instance$Holder$Attribute$scale, (Consumer<?>) (o) -> {});
                Reflections.method$AttributeInstance$setBaseValue.invoke(attributeInstance, this.scale);
                packets.accept(Reflections.constructor$ClientboundUpdateAttributesPacket0.newInstance(entityIds[1], Collections.singletonList(attributeInstance)), false);
            }
            if (this.direction == Direction.UP) {
                collider.accept(this.createCollider(this.direction));
                if (this.interactionEntity) {
                    packets.accept(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                            entityIds[2], UUID.randomUUID(), x + offset.x, y + offset.y - 0.005f, z - offset.z, 0, yaw,
                            Reflections.instance$EntityType$INTERACTION, 0, Reflections.instance$Vec3$Zero, 0
                    ), true);
                    packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityIds[2], List.copyOf(this.cachedInteractionValues)), true);
                }
            } else if (this.direction == Direction.DOWN) {
                collider.accept(this.createCollider(this.direction));
                packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityIds[1], List.of(ShulkerData.AttachFace.createEntityDataIfNotDefaultValue(Reflections.instance$Direction$UP))), false);
                if (this.interactionEntity) {
                    packets.accept(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                            entityIds[2], UUID.randomUUID(), x + offset.x, y + offset.y - 0.005f + this.yOffset, z - offset.z, 0, yaw,
                            Reflections.instance$EntityType$INTERACTION, 0, Reflections.instance$Vec3$Zero, 0
                    ), true);
                    packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityIds[2], List.copyOf(this.cachedInteractionValues)), true);
                }
            } else {
                Direction shulkerAnchor = getOriginalDirection(this.direction, Direction.fromYaw(yaw));
                collider.accept(this.createCollider(shulkerAnchor.opposite()));
                packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityIds[1], List.of(ShulkerData.AttachFace.createEntityDataIfNotDefaultValue(DirectionUtils.toNMSDirection(shulkerAnchor)))), false);
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
                    position, directionEnum,
                    scale, peek, interactionEntity, interactive
            );
        }
    }

    public static Direction getOriginalDirection(Direction newDirection, Direction oldDirection) {
        switch (newDirection) {
            case NORTH -> {
                return switch (oldDirection) {
                    case NORTH -> Direction.NORTH;
                    case SOUTH -> Direction.SOUTH;
                    case WEST -> Direction.EAST;
                    case EAST -> Direction.WEST;
                    default -> throw new IllegalStateException("Unexpected value: " + oldDirection);
                };
            }
            case SOUTH -> {
                return switch (oldDirection) {
                    case SOUTH -> Direction.NORTH;
                    case WEST -> Direction.WEST;
                    case EAST -> Direction.EAST;
                    case NORTH -> Direction.SOUTH;
                    default -> throw new IllegalStateException("Unexpected value: " + oldDirection);
                };
            }
            case WEST -> {
                return switch (oldDirection) {
                    case SOUTH -> Direction.EAST;
                    case WEST -> Direction.NORTH;
                    case EAST -> Direction.SOUTH;
                    case NORTH -> Direction.WEST;
                    default -> throw new IllegalStateException("Unexpected value: " + oldDirection);
                };
            }
            case EAST -> {
                return switch (oldDirection) {
                    case SOUTH -> Direction.WEST;
                    case WEST -> Direction.SOUTH;
                    case EAST -> Direction.NORTH;
                    case NORTH -> Direction.EAST;
                    default -> throw new IllegalStateException("Unexpected value: " + oldDirection);
                };
            }
            default -> throw new IllegalStateException("Unexpected value: " + newDirection);
        }
    }
}
