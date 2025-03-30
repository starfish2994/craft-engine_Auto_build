package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.LegacyAttributeUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.ArrayUtils;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.QuaternionUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.lang.ref.WeakReference;
import java.util.*;

public class LoadedFurniture {
    private final Key id;
    private final CustomFurniture furniture;
    private final AnchorType anchorType;
    // location
    private Location location;
    // base entity
    private final WeakReference<Entity> baseEntity;
    private final int baseEntityId;
    // cache
    private final List<Integer> fakeEntityIds;
    private final List<Integer> hitBoxEntityIds;
    private final Map<Integer, HitBox> hitBoxes;
    // seats
    private final Set<Vector3f> occupiedSeats = Collections.synchronizedSet(new HashSet<>());
    private final Vector<Entity> seats = new Vector<>();
    // cached spawn packet
    private Object cachedSpawnPacket;

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
        List<Integer> fakeEntityIds = new ArrayList<>();
        List<Integer> hitBoxEntityIds = new ArrayList<>();
        CustomFurniture.Placement placement = furniture.getPlacement(anchorType);

        List<Object> packets = new ArrayList<>();
        for (FurnitureElement element : placement.elements()) {
            int entityId = Reflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
            fakeEntityIds.add(entityId);
            element.addSpawnPackets(entityId, this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), packets::add);
        }
        for (HitBox hitBox : placement.hitBoxes()) {
            int[] ids = hitBox.acquireEntityIds(Reflections.instance$Entity$ENTITY_COUNTER::incrementAndGet);
            for (int entityId : ids) {
                fakeEntityIds.add(entityId);
                hitBoxEntityIds.add(entityId);
                hitBox.addSpawnPackets(ids, this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), packets::add);
                this.hitBoxes.put(entityId, hitBox);
            }
        }
        try {
            this.cachedSpawnPacket = Reflections.constructor$ClientboundBundlePacket.newInstance(packets);
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to init spawn packets for furniture " + id, e);
        }
        this.fakeEntityIds = fakeEntityIds;
        this.hitBoxEntityIds = hitBoxEntityIds;
    }

    @NotNull
    public Object spawnPacket() {
        return this.cachedSpawnPacket;
    }

    @NotNull
    public Location location() {
        return this.location;
    }

    @NotNull
    public Entity baseEntity() {
        Entity entity = baseEntity.get();
        if (entity == null) {
            throw new RuntimeException("Base entity not found. It might be unloaded.");
        }
        return entity;
    }

    public boolean isValid() {
        return baseEntity().isValid();
    }

    public void destroy() {
        if (!isValid()) {
            return;
        }
        this.baseEntity().remove();
        for (Entity entity : this.seats) {
            for (Entity passenger : entity.getPassengers()) {
                entity.removePassenger(passenger);
            }
            entity.remove();
        }
        this.seats.clear();
    }

    public void destroySeats() {
        for (Entity entity : this.seats) {
            entity.remove();
        }
        this.seats.clear();
    }

    public Optional<Seat> findFirstAvailableSeat(int targetEntityId) {
        HitBox hitbox = hitBoxes.get(targetEntityId);
        if (hitbox == null) return Optional.empty();

        Seat[] seats = hitbox.seats();
        if (ArrayUtils.isEmpty(seats)) return Optional.empty();

        return Arrays.stream(seats)
                .filter(s -> !occupiedSeats.contains(s.offset()))
                .findFirst();
    }

    public boolean removeOccupiedSeat(Vector3f seat) {
        return this.occupiedSeats.remove(seat);
    }

    public boolean removeOccupiedSeat(Seat seat) {
        return this.removeOccupiedSeat(seat.offset());
    }

    public boolean tryOccupySeat(Seat seat) {
        if (this.occupiedSeats.contains(seat.offset())) {
            return false;
        }
        this.occupiedSeats.add(seat.offset());
        return true;
    }

    public UUID uuid() {
        return this.baseEntity().getUniqueId();
    }

    public int baseEntityId() {
        return this.baseEntityId;
    }

    @NotNull
    public List<Integer> hitBoxEntityIds() {
        return Collections.unmodifiableList(this.hitBoxEntityIds);
    }

    @NotNull
    public List<Integer> subEntityIds() {
        return Collections.unmodifiableList(this.fakeEntityIds);
    }

    @NotNull
    public AnchorType anchorType() {
        return this.anchorType;
    }

    @NotNull
    public Key id() {
        return this.id;
    }

    @NotNull
    public CustomFurniture config() {
        return this.furniture;
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
