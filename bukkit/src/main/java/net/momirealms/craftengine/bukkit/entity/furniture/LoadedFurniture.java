package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.entity.BukkitEntity;
import net.momirealms.craftengine.bukkit.nms.CollisionEntity;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.LegacyAttributeUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.ArrayUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.QuaternionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.Vec3d;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.*;

public class LoadedFurniture implements Furniture {
    private final Key id;
    private final CustomFurniture furniture;
    private final AnchorType anchorType;
    // location
    private final Location location;
    // base entity
    private final WeakReference<Entity> baseEntity;
    private final int baseEntityId;
    // colliders
    private final CollisionEntity[] collisionEntities;
    // cache
    private final List<Integer> fakeEntityIds;
    private final List<Integer> entityIds;
    private final Map<Integer, HitBox> hitBoxes;
    private final boolean minimized;
    private final boolean hasExternalModel;
    // seats
    private final Set<Vector3f> occupiedSeats = Collections.synchronizedSet(new HashSet<>());
    private final Vector<Entity> seats = new Vector<>();
    // cached spawn packet
    private Object cachedSpawnPacket;
    private Object cachedMinimizedSpawnPacket;

    public LoadedFurniture(Entity baseEntity,
                           CustomFurniture furniture,
                           AnchorType anchorType) {
        this.id = furniture.id();
        this.baseEntityId = baseEntity.getEntityId();
        this.anchorType = anchorType;
        this.location = baseEntity.getLocation();
        this.baseEntity = new WeakReference<>(baseEntity);
        this.furniture = furniture;
        this.hitBoxes = new HashMap<>();
        this.minimized = furniture.settings().minimized();
        List<Integer> fakeEntityIds = new ArrayList<>();
        List<Integer> mainEntityIds = new ArrayList<>();
        mainEntityIds.add(this.baseEntityId);

        CustomFurniture.Placement placement = furniture.getPlacement(anchorType);
        // bind external furniture
        Optional<ExternalModel> optionalExternal = placement.externalModel();
        if (optionalExternal.isPresent()) {
            try {
                optionalExternal.get().bindModel(new BukkitEntity(baseEntity));
            } catch (Exception e) {
                CraftEngine.instance().logger().warn("Failed to load external model for furniture " + id, e);
            }
            hasExternalModel = true;
        } else {
            hasExternalModel = false;
        }

        double yawInRadius = Math.toRadians(180 - this.location.getYaw());
        Quaternionf conjugated = QuaternionUtils.toQuaternionf(0, yawInRadius, 0).conjugate();

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        float yaw = this.location.getYaw();

        List<Object> packets = new ArrayList<>();
        List<Object> minimizedPackets = new ArrayList<>();
        for (FurnitureElement element : placement.elements()) {
            int entityId = Reflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
            fakeEntityIds.add(entityId);
            element.addSpawnPackets(entityId, x, y, z, yaw, conjugated, packet -> {
                packets.add(packet);
                if (this.minimized) minimizedPackets.add(packet);
            });
        }
        for (HitBox hitBox : placement.hitBoxes()) {
            int[] ids = hitBox.acquireEntityIds(Reflections.instance$Entity$ENTITY_COUNTER::incrementAndGet);
            for (int entityId : ids) {
                fakeEntityIds.add(entityId);
                mainEntityIds.add(entityId);
                this.hitBoxes.put(entityId, hitBox);
            }
            hitBox.addSpawnPackets(ids, x, y, z, yaw, conjugated, (packet, canBeMinimized) -> {
                packets.add(packet);
                if (this.minimized && !canBeMinimized) {
                    minimizedPackets.add(packet);
                }
            });
        }
        try {
            this.cachedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(packets);
            if (this.minimized) {
                this.cachedMinimizedSpawnPacket = FastNMS.INSTANCE.constructor$ClientboundBundlePacket(minimizedPackets);
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to init spawn packets for furniture " + id, e);
        }
        this.fakeEntityIds = fakeEntityIds;
        this.entityIds = mainEntityIds;
        int colliderSize = placement.colliders().length;
        this.collisionEntities = new CollisionEntity[colliderSize];
        if (colliderSize != 0) {
            Object world = FastNMS.INSTANCE.field$CraftWorld$ServerLevel(this.location.getWorld());
            for (int i = 0; i < colliderSize; i++) {
                Collider collider = placement.colliders()[i];
                Vector3f offset = conjugated.transform(new Vector3f(collider.position()));
                Vector3d offset1 = collider.point1();
                Vector3d offset2 = collider.point2();
                double x1 = x + offset1.x() + offset.x();
                double x2 = x + offset2.x() + offset.x();
                double y1 = y + offset1.y() + offset.y();
                double y2 = y + offset2.y() + offset.y();
                double z1 = z + offset1.z() - offset.z();
                double z2 = z + offset2.z() - offset.z();
                Object aabb = FastNMS.INSTANCE.constructor$AABB(x1, y1, z1, x2, y2, z2);
                CollisionEntity entity = FastNMS.INSTANCE.createCollisionShulker(world, aabb, x, y, z, collider.canBeHitByProjectile());
                this.collisionEntities[i] = entity;
            }
        }
    }

    @Override
    public void initializeColliders() {
        Object world = FastNMS.INSTANCE.field$CraftWorld$ServerLevel(this.location.getWorld());
        for (CollisionEntity entity : this.collisionEntities) {
            FastNMS.INSTANCE.method$LevelWriter$addFreshEntity(world, entity);
            Entity bukkitEntity = FastNMS.INSTANCE.method$Entity$getBukkitEntity(entity);
            bukkitEntity.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_COLLISION, PersistentDataType.BYTE, (byte) 1);
        }
    }

    @NotNull
    public Object spawnPacket(Player player) {
        // TODO hasPermission might be slow, can we use a faster way in the future?
        if (!this.minimized || player.hasPermission(FurnitureManager.FURNITURE_ADMIN_NODE)) {
            return this.cachedSpawnPacket;
        } else {
            return this.cachedMinimizedSpawnPacket;
        }
    }

    @Override
    public Vec3d position() {
        return new Vec3d(location.getX(), location.getY(), location.getZ());
    }

    @Override
    public World world() {
        return new BukkitWorld(this.location.getWorld());
    }

    @NotNull
    public Location location() {
        return this.location.clone();
    }

    @NotNull
    public Entity baseEntity() {
        Entity entity = baseEntity.get();
        if (entity == null) {
            throw new RuntimeException("Base entity not found. It might be unloaded.");
        }
        return entity;
    }

    @Override
    public boolean isValid() {
        return baseEntity().isValid();
    }

    @Override
    public void destroy() {
        if (!isValid()) {
            return;
        }
        this.baseEntity().remove();
        for (CollisionEntity entity : this.collisionEntities) {
            if (entity != null)
                entity.destroy();
        }
        for (Entity entity : this.seats) {
            for (Entity passenger : entity.getPassengers()) {
                entity.removePassenger(passenger);
            }
            entity.remove();
        }
        this.seats.clear();
    }

    @Override
    public void destroySeats() {
        for (Entity entity : this.seats) {
            entity.remove();
        }
        this.seats.clear();
    }

    @Override
    public Optional<Seat> findFirstAvailableSeat(int targetEntityId) {
        HitBox hitbox = hitBoxes.get(targetEntityId);
        if (hitbox == null) return Optional.empty();

        Seat[] seats = hitbox.seats();
        if (ArrayUtils.isEmpty(seats)) return Optional.empty();

        return Arrays.stream(seats)
                .filter(s -> !occupiedSeats.contains(s.offset()))
                .findFirst();
    }

    @Override
    public boolean removeOccupiedSeat(Vector3f seat) {
        return this.occupiedSeats.remove(seat);
    }

    @Override
    public boolean tryOccupySeat(Seat seat) {
        if (this.occupiedSeats.contains(seat.offset())) {
            return false;
        }
        this.occupiedSeats.add(seat.offset());
        return true;
    }

    @Override
    public UUID uuid() {
        return this.baseEntity().getUniqueId();
    }

    @Override
    public int baseEntityId() {
        return this.baseEntityId;
    }

    @NotNull
    public List<Integer> entityIds() {
        return Collections.unmodifiableList(this.entityIds);
    }

    @NotNull
    public List<Integer> fakeEntityIds() {
        return Collections.unmodifiableList(this.fakeEntityIds);
    }

    public CollisionEntity[] collisionEntities() {
        return this.collisionEntities;
    }

    @Override
    public @NotNull AnchorType anchorType() {
        return this.anchorType;
    }

    @Override
    public @NotNull Key id() {
        return this.id;
    }

    @Override
    public @NotNull CustomFurniture config() {
        return this.furniture;
    }

    @Override
    public boolean hasExternalModel() {
        return hasExternalModel;
    }

    @Override
    public void spawnSeatEntityForPlayer(net.momirealms.craftengine.core.entity.player.Player player, Seat seat) {
        spawnSeatEntityForPlayer((Player) player.platformPlayer(), seat);
    }

    public void spawnSeatEntityForPlayer(org.bukkit.entity.Player player, Seat seat) {
        Location location = this.calculateSeatLocation(seat);
        Entity seatEntity = seat.limitPlayerRotation() ?
                EntityUtils.spawnEntity(player.getWorld(), VersionHelper.isVersionNewerThan1_20_2() ? location.subtract(0,0.9875,0) : location.subtract(0,0.990625,0), EntityType.ARMOR_STAND, entity -> {
                    ArmorStand armorStand = (ArmorStand) entity;
                    if (VersionHelper.isVersionNewerThan1_21_3()) {
                        Objects.requireNonNull(armorStand.getAttribute(Attribute.MAX_HEALTH)).setBaseValue(0.01);
                    } else {
                        LegacyAttributeUtils.setMaxHealth(armorStand);
                    }
                    armorStand.setSmall(true);
                    armorStand.setInvisible(true);
                    armorStand.setSilent(true);
                    armorStand.setInvulnerable(true);
                    armorStand.setArms(false);
                    armorStand.setCanTick(false);
                    armorStand.setAI(false);
                    armorStand.setGravity(false);
                    armorStand.setPersistent(false);
                    armorStand.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_SEAT_BASE_ENTITY_KEY, PersistentDataType.INTEGER, this.baseEntityId());
                    armorStand.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_SEAT_VECTOR_3F_KEY, PersistentDataType.STRING, seat.offset().x + ", " + seat.offset().y + ", " + seat.offset().z);
                }) :
                EntityUtils.spawnEntity(player.getWorld(), VersionHelper.isVersionNewerThan1_20_2() ? location : location.subtract(0,0.25,0), EntityType.ITEM_DISPLAY, entity -> {
                    ItemDisplay itemDisplay = (ItemDisplay) entity;
                    itemDisplay.setPersistent(false);
                    itemDisplay.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_SEAT_BASE_ENTITY_KEY, PersistentDataType.INTEGER, this.baseEntityId());
                    itemDisplay.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_SEAT_VECTOR_3F_KEY, PersistentDataType.STRING, seat.offset().x + ", " + seat.offset().y + ", " + seat.offset().z);
                });
        this.seats.add(seatEntity);
        seatEntity.addPassenger(player);
    }

    private Location calculateSeatLocation(Seat seat) {
        Vector3f offset = QuaternionUtils.toQuaternionf(0, Math.toRadians(180 - this.location.getYaw()), 0).conjugate().transform(new Vector3f(seat.offset()));
        double yaw = seat.yaw() + this.location.getYaw();
        if (yaw < -180) yaw += 360;
        Location newLocation = this.location.clone();
        newLocation.setYaw((float) yaw);
        newLocation.add(offset.x, offset.y + 0.6, -offset.z);
        return newLocation;
    }
}
