package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.compatibility.bettermodel.BetterModelModel;
import net.momirealms.craftengine.bukkit.compatibility.modelengine.ModelEngineModel;
import net.momirealms.craftengine.bukkit.entity.furniture.hitbox.InteractionHitBox;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.util.EntityUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.entity.furniture.*;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigManager;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;
import net.momirealms.craftengine.core.sound.SoundData;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;
import org.incendo.cloud.suggestion.Suggestion;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BukkitFurnitureManager implements FurnitureManager {
    public static final NamespacedKey FURNITURE_KEY = Objects.requireNonNull(NamespacedKey.fromString("craftengine:furniture_id"));
    public static final NamespacedKey FURNITURE_ANCHOR_KEY = Objects.requireNonNull(NamespacedKey.fromString("craftengine:anchor_type"));
    public static final NamespacedKey FURNITURE_SEAT_BASE_ENTITY_KEY = Objects.requireNonNull(NamespacedKey.fromString("craftengine:seat_to_base_entity"));
    public static final NamespacedKey FURNITURE_SEAT_VECTOR_3F_KEY = Objects.requireNonNull(NamespacedKey.fromString("craftengine:seat_vector"));
    private static BukkitFurnitureManager instance;
    private final BukkitCraftEngine plugin;

    private final Map<Key, CustomFurniture> byId = new HashMap<>();

    private final Map<Integer, LoadedFurniture> furnitureByBaseEntityId  = new ConcurrentHashMap<>(256, 0.5f);
    private final Map<Integer, LoadedFurniture> furnitureByEntityId = new ConcurrentHashMap<>(512, 0.5f);
    // Event listeners
    private final Listener dismountListener;
    private final FurnitureEventListener furnitureEventListener;
    // tick task
    private SchedulerTask tickTask;
    // Cached command suggestions
    private final List<Suggestion> cachedSuggestions = new ArrayList<>();

    public static BukkitFurnitureManager instance() {
        return instance;
    }

    public BukkitFurnitureManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        this.furnitureEventListener = new FurnitureEventListener(this);
        this.dismountListener = VersionHelper.isVersionNewerThan1_20_3() ? new DismountListener1_20_3(this) : new DismountListener1_20(this::handleDismount);
        instance = this;
    }

    public LoadedFurniture place(CustomFurniture furniture, Location location, AnchorType anchorType, boolean playSound) {
        if (furniture.isAllowedPlacement(anchorType)) {
            anchorType = furniture.getAnyPlacement();
        }
        AnchorType finalAnchorType = anchorType;
        Entity furnitureEntity = EntityUtils.spawnEntity(location.getWorld(), location, EntityType.ITEM_DISPLAY, entity -> {
            ItemDisplay display = (ItemDisplay) entity;
            display.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_KEY, PersistentDataType.STRING, furniture.id().toString());
            display.getPersistentDataContainer().set(BukkitFurnitureManager.FURNITURE_ANCHOR_KEY, PersistentDataType.STRING, finalAnchorType.name());
            handleEntityLoadEarly(display);
        });
        if (playSound) {
            SoundData data = furniture.settings().sounds().placeSound();
            location.getWorld().playSound(location, data.id().toString(), SoundCategory.BLOCKS, data.volume(), data.pitch());
        }
        return getLoadedFurnitureByBaseEntityId(furnitureEntity.getEntityId());
    }

    @Override
    public void delayedLoad() {
        this.initSuggestions();
    }

    @Override
    public void initSuggestions() {
        this.cachedSuggestions.clear();
        for (Key key : this.byId.keySet()) {
            this.cachedSuggestions.add(Suggestion.suggestion(key.toString()));
        }
    }

    @Override
    public Collection<Suggestion> cachedSuggestions() {
        return Collections.unmodifiableCollection(this.cachedSuggestions);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
        Map<String, Object> lootMap = MiscUtils.castToMap(section.get("loot"), true);
        Map<String, Object> settingsMap = MiscUtils.castToMap(section.get("settings"), true);
        Map<String, Object> placementMap = MiscUtils.castToMap(section.get("placement"), true);
        EnumMap<AnchorType, CustomFurniture.Placement> placements = new EnumMap<>(AnchorType.class);
        if (placementMap == null) {
            throw new IllegalArgumentException("Missing required parameter 'placement' for furniture " + id);
        }

        for (Map.Entry<String, Object> entry : placementMap.entrySet()) {
            // anchor type
            AnchorType anchorType = AnchorType.valueOf(entry.getKey().toUpperCase(Locale.ENGLISH));
            Map<String, Object> placementArguments = MiscUtils.castToMap(entry.getValue(), true);

            // furniture display elements
            List<FurnitureElement> elements = new ArrayList<>();
            List<Map<String, Object>> elementConfigs = (List<Map<String, Object>>) placementArguments.getOrDefault("elements", List.of());
            for (Map<String, Object> element : elementConfigs) {
                String key = (String) element.get("item");
                if (key == null) {
                    throw new IllegalArgumentException("Missing required parameter 'item' for furniture " + id);
                }
                ItemDisplayContext transform = ItemDisplayContext.valueOf(element.getOrDefault("transform", "NONE").toString().toUpperCase(Locale.ENGLISH));
                Billboard billboard = Billboard.valueOf(element.getOrDefault("billboard", "FIXED").toString().toUpperCase(Locale.ENGLISH));
                FurnitureElement furnitureElement = new BukkitFurnitureElement(Key.of(key), billboard, transform,
                        MiscUtils.getVector3f(element.getOrDefault("scale", "1")),
                        MiscUtils.getVector3f(element.getOrDefault("translation", "0")),
                        MiscUtils.getVector3f(element.getOrDefault("position", "0")),
                        MiscUtils.getQuaternionf(element.getOrDefault("rotation", "0"))
                );
                elements.add(furnitureElement);
            }

            // add colliders
            List<Map<String, Object>> colliderConfigs = (List<Map<String, Object>>) placementArguments.getOrDefault("colliders", List.of());
            List<Collider> colliders = new ArrayList<>();
            for (Map<String, Object> config : colliderConfigs) {
                if (!config.containsKey("position")) {
                    colliders.add(new Collider(
                            (boolean) config.getOrDefault("can-be-hit-by-projectile", false),
                            MiscUtils.getVector3d(config.getOrDefault("point-1", "0")),
                            MiscUtils.getVector3d(config.getOrDefault("point-2", "0"))
                    ));
                } else {
                    colliders.add(new Collider(
                            (boolean) config.getOrDefault("can-be-hit-by-projectile", false),
                            MiscUtils.getVector3f(config.getOrDefault("position", "0")),
                            MiscUtils.getAsFloat(config.getOrDefault("width", "1")),
                            MiscUtils.getAsFloat(config.getOrDefault("height", "1"))
                    ));
                }
            }

            // external model providers
            Optional<ExternalModel> externalModel;
            if (placementArguments.containsKey("model-engine")) {
                externalModel = Optional.of(new ModelEngineModel(placementArguments.get("model-engine").toString()));
            } else if (placementArguments.containsKey("better-model")) {
                externalModel = Optional.of(new BetterModelModel(placementArguments.get("better-model").toString()));
            } else {
                externalModel = Optional.empty();
            }

            // add hitboxes
            List<Map<String, Object>> hitboxConfigs = (List<Map<String, Object>>) placementArguments.getOrDefault("hitboxes", List.of());
            List<HitBox> hitboxes = new ArrayList<>();
            for (Map<String, Object> config : hitboxConfigs) {
                HitBox hitBox = HitBoxTypes.fromMap(config);
                hitboxes.add(hitBox);
                hitBox.optionCollider().ifPresent(colliders::add);
            }
            if (hitboxes.isEmpty() && externalModel.isEmpty()) {
                hitboxes.add(InteractionHitBox.DEFAULT);
            }

            // rules
            Map<String, Object> ruleSection = MiscUtils.castToMap(placementArguments.get("rules"), true);
            if (ruleSection != null) {
                RotationRule rotationRule = Optional.ofNullable((String) ruleSection.get("rotation"))
                        .map(it -> RotationRule.valueOf(it.toUpperCase(Locale.ENGLISH)))
                        .orElse(RotationRule.ANY);
                AlignmentRule alignmentRule = Optional.ofNullable((String) ruleSection.get("alignment"))
                        .map(it -> AlignmentRule.valueOf(it.toUpperCase(Locale.ENGLISH)))
                        .orElse(AlignmentRule.CENTER);
                placements.put(anchorType, new CustomFurniture.Placement(
                        elements.toArray(new FurnitureElement[0]),
                        hitboxes.toArray(new HitBox[0]),
                        colliders.toArray(new Collider[0]),
                        rotationRule,
                        alignmentRule,
                        externalModel
                ));
            } else {
                placements.put(anchorType, new CustomFurniture.Placement(
                        elements.toArray(new FurnitureElement[0]),
                        hitboxes.toArray(new HitBox[0]),
                        colliders.toArray(new Collider[0]),
                        RotationRule.ANY,
                        AlignmentRule.CENTER,
                        externalModel
                ));
            }
        }

        CustomFurniture furniture = new CustomFurniture(
                id,
                FurnitureSettings.fromMap(settingsMap),
                placements,
                lootMap == null ? null : LootTable.fromMap(lootMap)
        );

        this.byId.put(id, furniture);
    }

    public void tick() {
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this.dismountListener, this.plugin.bootstrap());
        Bukkit.getPluginManager().registerEvents(this.furnitureEventListener, this.plugin.bootstrap());
        this.tickTask = plugin.scheduler().sync().runRepeating(this::tick, 1, 1);
        for (World world : Bukkit.getWorlds()) {
            List<Entity> entities = world.getEntities();
            for (Entity entity : entities) {
                handleEntityLoadEarly(entity);
            }
        }
    }

    @Override
    public void unload() {
        this.byId.clear();
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this.dismountListener);
        HandlerList.unregisterAll(this.furnitureEventListener);
        if (tickTask != null && !tickTask.cancelled()) {
            tickTask.cancel();
        }
        unload();
        for (Player player : Bukkit.getOnlinePlayers()) {
            Entity vehicle = player.getVehicle();
            if (vehicle != null) {
                tryLeavingSeat(player, vehicle);
            }
        }
    }

    @Override
    public Optional<CustomFurniture> getFurniture(Key id) {
        return Optional.ofNullable(this.byId.get(id));
    }

    @Override
    public boolean isFurnitureBaseEntity(int entityId) {
        return this.furnitureByBaseEntityId.containsKey(entityId);
    }

    @Nullable
    public LoadedFurniture getLoadedFurnitureByBaseEntityId(int entityId) {
        return this.furnitureByBaseEntityId.get(entityId);
    }

    @Nullable
    public LoadedFurniture getLoadedFurnitureByEntityId(int entityId) {
        return this.furnitureByEntityId.get(entityId);
    }

    protected void handleBaseFurnitureUnload(Entity entity) {
        int id = entity.getEntityId();
        LoadedFurniture furniture = this.furnitureByBaseEntityId.remove(id);
        if (furniture != null) {
            furniture.destroySeats();
            for (int sub : furniture.entityIds()) {
                this.furnitureByEntityId.remove(sub);
            }
        }
    }

    @SuppressWarnings("deprecation") // just a misleading name `getTrackedPlayers`
    protected void handleEntityLoadLate(Entity entity) {
        if (entity instanceof ItemDisplay display) {
            String id = entity.getPersistentDataContainer().get(FURNITURE_KEY, PersistentDataType.STRING);
            if (id == null) return;
            Key key = Key.of(id);
            Optional<CustomFurniture> optionalFurniture = getFurniture(key);
            if (optionalFurniture.isEmpty()) return;
            CustomFurniture customFurniture = optionalFurniture.get();
            LoadedFurniture previous = this.furnitureByBaseEntityId.get(display.getEntityId());
            if (previous != null) return;
            Location location = entity.getLocation();
            if (FastNMS.INSTANCE.isPreventingStatusUpdates(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                return;
            }
            LoadedFurniture furniture = addNewFurniture(display, customFurniture, getAnchorType(entity, customFurniture));
            for (Player player : display.getTrackedPlayers()) {
                this.plugin.adapt(player).furnitureView().computeIfAbsent(furniture.baseEntityId(), k -> new ArrayList<>()).addAll(furniture.fakeEntityIds());
                this.plugin.networkManager().sendPacket(player, furniture.spawnPacket(player));
            }
        }
    }

    public void handleEntityLoadEarly(Entity entity) {
        if (entity instanceof ItemDisplay display) {
            String id = entity.getPersistentDataContainer().get(FURNITURE_KEY, PersistentDataType.STRING);
            if (id == null) return;
            Key key = Key.of(id);
            Optional<CustomFurniture> optionalFurniture = getFurniture(key);
            if (optionalFurniture.isPresent()) {
                CustomFurniture customFurniture = optionalFurniture.get();
                LoadedFurniture previous = this.furnitureByBaseEntityId.get(display.getEntityId());
                if (previous != null) return;
                addNewFurniture(display, customFurniture, getAnchorType(entity, customFurniture));
                return;
            }
            // Remove the entity if it's not a valid furniture
            if (ConfigManager.removeInvalidFurniture()) {
                if (ConfigManager.furnitureToRemove().isEmpty() || ConfigManager.furnitureToRemove().contains(id)) {
                    entity.remove();
                }
            }
        }
    }

    private AnchorType getAnchorType(Entity baseEntity, CustomFurniture furniture) {
        String anchorType = baseEntity.getPersistentDataContainer().get(FURNITURE_ANCHOR_KEY, PersistentDataType.STRING);
        if (anchorType != null) {
            try {
                AnchorType unverified = AnchorType.valueOf(anchorType);
                if (furniture.isAllowedPlacement(unverified)) {
                    return unverified;
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        AnchorType anchorTypeEnum = furniture.getAnyPlacement();
        baseEntity.getPersistentDataContainer().set(FURNITURE_ANCHOR_KEY, PersistentDataType.STRING, anchorTypeEnum.name());
        return anchorTypeEnum;
    }

    private synchronized LoadedFurniture addNewFurniture(ItemDisplay display, CustomFurniture furniture, AnchorType anchorType) {
        LoadedFurniture loadedFurniture = new LoadedFurniture(display, furniture, anchorType);
        this.furnitureByBaseEntityId.put(loadedFurniture.baseEntityId(), loadedFurniture);
        for (int entityId : loadedFurniture.entityIds()) {
            this.furnitureByEntityId.put(entityId, loadedFurniture);
        }
        return loadedFurniture;
    }

    protected void handleDismount(Player player, Entity entity) {
        if (!isSeatCarrierType(entity)) return;
        Location location = entity.getLocation();
        plugin.scheduler().sync().runDelayed(() -> tryLeavingSeat(player, entity), player.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    protected void tryLeavingSeat(@NotNull Player player, @NotNull Entity vehicle) {
        Integer baseFurniture = vehicle.getPersistentDataContainer().get(FURNITURE_SEAT_BASE_ENTITY_KEY, PersistentDataType.INTEGER);
        if (baseFurniture == null) return;
        vehicle.remove();
        LoadedFurniture furniture = getLoadedFurnitureByBaseEntityId(baseFurniture);
        if (furniture == null) {
            return;
        }
        String vector3f = vehicle.getPersistentDataContainer().get(BukkitFurnitureManager.FURNITURE_SEAT_VECTOR_3F_KEY, PersistentDataType.STRING);
        if (vector3f == null) {
            plugin.logger().warn("Failed to get vector3f for player " + player.getName() + "'s seat");
            return;
        }
        Vector3f seatPos = MiscUtils.getVector3f(vector3f);
        furniture.removeOccupiedSeat(seatPos);

        Location vehicleLocation = vehicle.getLocation();
        Location originalLocation = vehicleLocation.clone();
        originalLocation.setY(furniture.location().getY());
        Location targetLocation = originalLocation.clone().add(vehicleLocation.getDirection());
        if (!isSafeLocation(targetLocation)) {
            targetLocation = findSafeLocationNearby(originalLocation);
            if (targetLocation == null) return;
        }
        targetLocation.setYaw(player.getLocation().getYaw());
        targetLocation.setPitch(player.getLocation().getPitch());
        player.teleport(targetLocation);
    }

    protected boolean isSeatCarrierType(Entity entity) {
        return (entity instanceof ArmorStand || entity instanceof ItemDisplay);
    }

    private boolean isSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return false;
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (!world.getBlockAt(x, y - 1, z).getType().isSolid()) return false;
        if (!world.getBlockAt(x, y, z).isPassable()) return false;
        if (isEntityBlocking(location)) return false;
        return world.getBlockAt(x, y + 1, z).isPassable();
    }

    @Nullable
    private Location findSafeLocationNearby(Location center) {
        World world = center.getWorld();
        if (world == null) return null;
        int centerX = center.getBlockX();
        int centerY = center.getBlockY();
        int centerZ = center.getBlockZ();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                int x = centerX + dx;
                int z = centerZ + dz;
                Location nearbyLocation = new Location(world, x + 0.5, centerY, z + 0.5);
                if (isSafeLocation(nearbyLocation)) return nearbyLocation;
            }
        }
        return null;
    }

    private boolean isEntityBlocking(Location location) {
        World world = location.getWorld();
        if (world == null) return true;
        try {
            Collection<Entity> nearbyEntities = world.getNearbyEntities(location, 0.5, 2, 0.5);
            for (Entity bukkitEntity : nearbyEntities) {
                if (bukkitEntity instanceof Player) continue;
                Object nmsEntity = FastNMS.INSTANCE.method$CraftEntity$getHandle(bukkitEntity);
                return (boolean) Reflections.method$Entity$canBeCollidedWith.invoke(nmsEntity);
            }
        } catch (Exception ignored) {}
        return false;
    }
}
