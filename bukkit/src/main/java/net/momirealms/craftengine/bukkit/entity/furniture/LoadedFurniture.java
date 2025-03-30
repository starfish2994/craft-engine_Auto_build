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
    private final Map<Integer, FurnitureElement> elements;
    private final Map<Integer, HitBox> hitBoxes;
    // location
    private Location location;
    // base entity
    private final WeakReference<Entity> baseEntity;
    private final int baseEntityId;
    // includes elements + interactions
    private final List<Integer> subEntityIds;
    // interactions
    private final List<Integer> interactionEntityIds;
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
        this.elements = new HashMap<>();
        List<Integer> entityIds = new ArrayList<>();
        List<Integer> hitBoxEntityIds = new ArrayList<>();
        CustomFurniture.Placement placement = furniture.getPlacement(anchorType);
        for (FurnitureElement element : placement.elements()) {
            int entityId = Reflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
            entityIds.add(entityId);
            this.elements.put(entityId, element);
        }
        for (HitBox hitBox : placement.hitboxes()) {
            int entityId = Reflections.instance$Entity$ENTITY_COUNTER.incrementAndGet();
            entityIds.add(entityId);
            hitBoxEntityIds.add(entityId);
            this.hitBoxes.put(entityId, hitBox);
        }
        this.subEntityIds = entityIds;
        this.interactionEntityIds = hitBoxEntityIds;
    }

    public synchronized Object spawnPacket() {
        if (this.cachedSpawnPacket == null) {
            try {
                List<Object> packets = new ArrayList<>();
                for (Map.Entry<Integer, FurnitureElement> entry : this.elements.entrySet()) {
                    entry.getValue().addSpawnPackets(entry.getKey(), this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), packets::add);
                }
                for (Map.Entry<Integer, HitBox> entry : this.hitBoxes.entrySet()) {
                    entry.getValue().addSpawnPackets(entry.getKey(), this.location.getX(), this.location.getY(), this.location.getZ(), this.location.getYaw(), packets::add);
                }
                this.cachedSpawnPacket = Reflections.constructor$ClientboundBundlePacket.newInstance(packets);
            } catch (Exception e) {
                CraftEngine.instance().logger().warn("Failed to init spawn packets for furniture " + id, e);
            }
        }
        return this.cachedSpawnPacket;
    }

    @NotNull
    public Location location() {
        return this.location;
    }

    public void teleport(@NotNull Location location) {
        if (location.equals(this.location)) return;
        this.location = location;
    }

    @NotNull
    public Entity baseEntity() {
        Entity entity = baseEntity.get();
        if (entity == null) {
            throw new RuntimeException("Base entity not found");
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

    public void addSeatEntity(Entity entity) {
        this.seats.add(entity);
    }

    public Optional<Seat> getAvailableSeat(int clickedEntityId) {
        HitBox hitbox = this.hitBoxes.get(clickedEntityId);
        if (hitbox == null)
            return Optional.empty();
        Seat[] seats = hitbox.seats();
        if (ArrayUtils.isEmpty(seats)) {
            return Optional.empty();
        }
        for (Seat seat : seats) {
            if (!this.occupiedSeats.contains(seat.offset())) {
                return Optional.of(seat);
            }
        }
        return Optional.empty();
    }

    public Location getSeatLocation(Seat seat) {
        Vector3f offset = QuaternionUtils.toQuaternionf(0, Math.toRadians(180 - this.location.getYaw()), 0).conjugate().transform(new Vector3f(seat.offset()));
        double yaw = seat.yaw() + this.location.getYaw();
        if (yaw < -180) yaw += 360;
        Location newLocation = this.location.clone();
        newLocation.setYaw((float) yaw);
        newLocation.add(offset.x, offset.y + 0.6, -offset.z);
        return newLocation;
    }

    public boolean releaseSeat(Vector3f seat) {
        return this.occupiedSeats.remove(seat);
    }

    public boolean occupySeat(Seat seat) {
        if (this.occupiedSeats.contains(seat.offset())) {
            return false;
        }
        this.occupiedSeats.add(seat.offset());
        return true;
    }

    public int baseEntityId() {
        return baseEntityId;
    }

    public List<Integer> interactionEntityIds() {
        return interactionEntityIds;
    }

    public List<Integer> subEntityIds() {
        return this.subEntityIds;
    }

    public AnchorType anchorType() {
        return anchorType;
    }

    public Key furnitureId() {
        return id;
    }

    public CustomFurniture furniture() {
        return furniture;
    }

    public void mountSeat(org.bukkit.entity.Player player, Seat seat) {
        Location location = this.getSeatLocation(seat);
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
        this.addSeatEntity(seatEntity);
        seatEntity.addPassenger(player);
    }
}
