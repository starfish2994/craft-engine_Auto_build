package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.network.NMSPacketEvent;
import net.momirealms.craftengine.core.item.Enchantment;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MCUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CustomTridentUtils {
    public static final NamespacedKey customTridentKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:custom_trident"));
    public static final NamespacedKey interpolationDurationaKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:interpolation_duration"));
    public static final NamespacedKey displayTypeKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:display_type"));
    public static final NamespacedKey translationKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:translation"));
    public static final NamespacedKey rotationLeftKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:rotation_left"));

    public static void handleCustomTrident(NetWorkUser user, NMSPacketEvent event, Object packet) throws IllegalAccessException {
        int entityId = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$entityId(packet);
        Trident trident = getTridentById(user, entityId);
        if (trident == null) return;
        World world = trident.getWorld();
        Object serverEntity;
        Object nmsEntity = FastNMS.INSTANCE.method$CraftEntity$getHandle(trident);
        Object tracker = Reflections.field$Entity$trackedEntity.get(nmsEntity);
        if (tracker != null) {
            serverEntity = Reflections.field$ChunkMap$TrackedEntity$serverEntity.get(tracker);
        } else {
            serverEntity = null;
        }
        if (notCustomTrident(trident)) return;
        modifyCustomTridentPacket(packet);
        List<Object> itemDisplayValues = buildEntityDataValues(trident);
        user.tridentView().put(entityId, itemDisplayValues);
        user.sendPacket(packet, true);
        user.sendPacket(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityId, itemDisplayValues), true);
        event.setCancelled(true);
        if (serverEntity != null) {
            // 这里直接暴力更新
            SchedulerTask task = CraftEngine.instance().scheduler().asyncRepeating(() -> {
                try {
                    Reflections.method$ServerEntity$sendChanges.invoke(serverEntity);
                    if (!isInGround(nmsEntity)) {
                        world.spawnParticle(ParticleUtils.getParticle("BUBBLE"), trident.getLocation(), 1, 0, 0, 0, 0);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    CraftEngine.instance().logger().warn("Failed to send entity data packet", e);
                }
            }, 0, 5, TimeUnit.MILLISECONDS);
            user.tridentTaskView().put(entityId, task);
        }
    }

    private static boolean isInGround(Object nmsEntity) throws IllegalAccessException, InvocationTargetException {
        if (!Reflections.field$Entity$wasTouchingWater.getBoolean(nmsEntity)) return true;
        if (VersionHelper.isOrAbove1_21_2()) {
            return (boolean) Reflections.method$AbstractArrow$isInGround.invoke(nmsEntity);
        } else {
            return Reflections.field$AbstractArrow$inGround.getBoolean(nmsEntity);
        }
    }

    @Nullable
    public static Trident getTridentById(NetWorkUser user, int entityId) {
        Player player = (Player) user.platformPlayer();
        Entity entity = FastNMS.INSTANCE.getBukkitEntityById(player.getWorld(), entityId);
        if (entity instanceof Trident trident) return trident;
        return null;
    }

    public static boolean notCustomTrident(Trident trident) {
        if (trident == null) return true;
        PersistentDataContainer container = trident.getItemStack().getItemMeta().getPersistentDataContainer();
        String customTrident = container.get(customTridentKey, PersistentDataType.STRING);
        return customTrident == null;
    }

    public static void modifyCustomTridentPacket(Object packet) throws IllegalAccessException {
        float yRot = MCUtils.unpackDegrees(Reflections.field$ClientboundAddEntityPacket$yRot.getByte(packet));
        float xRot = MCUtils.unpackDegrees(Reflections.field$ClientboundAddEntityPacket$xRot.getByte(packet));
        Reflections.field$ClientboundAddEntityPacket$type.set(packet, Reflections.instance$EntityType$ITEM_DISPLAY);
        Reflections.field$ClientboundAddEntityPacket$yRot.setByte(packet, MCUtils.packDegrees(-yRot));
        Reflections.field$ClientboundAddEntityPacket$xRot.setByte(packet, MCUtils.packDegrees(Math.clamp(-xRot, -90.0F, 90.0F)));
    }

    public static List<Object> buildEntityDataValues(Trident trident) {
        List<Object> itemDisplayValues = new ArrayList<>();
        ItemStack itemStack = trident.getItemStack();
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        String customTrident = container.getOrDefault(customTridentKey, PersistentDataType.STRING, "craftengine:empty");
        Item<ItemStack> item = BukkitItemManager.instance().createWrappedItem(Key.of(customTrident), null);
        itemStack.getEnchantments().forEach((enchantment, level) -> item.addEnchantment(new Enchantment(Key.of(enchantment.getKey().toString()), level)));
        Integer interpolationDurationa = container.getOrDefault(interpolationDurationaKey, PersistentDataType.INTEGER, 2);
        Byte displayType = container.getOrDefault(displayTypeKey, PersistentDataType.BYTE, (byte) 0);
        String translation = container.getOrDefault(translationKey, PersistentDataType.STRING, "0+0+0");
        String[] translations = translation.split("\\+");
        String rotationLeft = container.getOrDefault(rotationLeftKey, PersistentDataType.STRING, "0+0+0+0");
        String[] rotationLefts = rotationLeft.split("\\+");
        ItemDisplayEntityData.InterpolationDelay.addEntityDataIfNotDefaultValue(-1, itemDisplayValues);
        ItemDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(new Vector3f(Float.parseFloat(translations[0]), Float.parseFloat(translations[1]), Float.parseFloat(translations[2])), itemDisplayValues);
        ItemDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(new Quaternionf(Float.parseFloat(rotationLefts[0]), Float.parseFloat(rotationLefts[1]), Float.parseFloat(rotationLefts[2]), Float.parseFloat(rotationLefts[3])), itemDisplayValues);
        if (VersionHelper.isOrAbove1_20_2()) {
            ItemDisplayEntityData.TransformationInterpolationDuration.addEntityDataIfNotDefaultValue(interpolationDurationa, itemDisplayValues);
            ItemDisplayEntityData.PositionRotationInterpolationDuration.addEntityDataIfNotDefaultValue(interpolationDurationa, itemDisplayValues);
        } else {
            ItemDisplayEntityData.InterpolationDuration.addEntityDataIfNotDefaultValue(interpolationDurationa, itemDisplayValues);
        }
        ItemDisplayEntityData.DisplayedItem.addEntityDataIfNotDefaultValue(item.getLiteralObject(), itemDisplayValues);
        ItemDisplayEntityData.DisplayType.addEntityDataIfNotDefaultValue(displayType, itemDisplayValues);
        return itemDisplayValues;
    }

    public static void modifyCustomTridentPositionSync(NMSPacketEvent event, Object packet, int entityId) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object positionMoveRotation = Reflections.field$ClientboundEntityPositionSyncPacket$values.get(packet);
        boolean onGround = Reflections.field$ClientboundEntityPositionSyncPacket$onGround.getBoolean(packet);
        Object position = Reflections.field$PositionMoveRotation$position.get(positionMoveRotation);
        Object deltaMovement = Reflections.field$PositionMoveRotation$deltaMovement.get(positionMoveRotation);
        float yRot = Reflections.field$PositionMoveRotation$yRot.getFloat(positionMoveRotation);
        float xRot = Reflections.field$PositionMoveRotation$xRot.getFloat(positionMoveRotation);
        Object newPositionMoveRotation = Reflections.constructor$PositionMoveRotation.newInstance(position, deltaMovement, -yRot, Math.clamp(-xRot, -90.0F, 90.0F));
        event.replacePacket(Reflections.constructor$ClientboundEntityPositionSyncPacket.newInstance(entityId, newPositionMoveRotation, onGround));
    }

    public static void modifyCustomTridentMove(Object packet) throws IllegalAccessException {
        float xRot = MCUtils.unpackDegrees(Reflections.field$ClientboundMoveEntityPacket$xRot.getByte(packet));
        float yRot = MCUtils.unpackDegrees(Reflections.field$ClientboundMoveEntityPacket$yRot.getByte(packet));
        Reflections.field$ClientboundMoveEntityPacket$xRot.setByte(packet, MCUtils.packDegrees(Math.clamp(-xRot, -90.0F, 90.0F)));
        Reflections.field$ClientboundMoveEntityPacket$yRot.setByte(packet, MCUtils.packDegrees(-yRot));
    }

    public static List<Object> buildCustomTridentSetEntityDataPacket(NetWorkUser user, List<Object> packedItems, int entityId) {
        List<Object> newPackedItems = new ArrayList<>();
        for (Object packedItem : packedItems) {
            int entityDataId = FastNMS.INSTANCE.field$SynchedEntityData$DataValue$id(packedItem);
            if (entityDataId < 8) {
                newPackedItems.add(packedItem);
            }
        }
        List<Object> newData = user.tridentView().getOrDefault(entityId, List.of());
        if (newData.isEmpty()) {
            Trident trident = getTridentById(user, entityId);
            if (notCustomTrident(trident)) return newPackedItems;
            newData = buildEntityDataValues(trident);
            user.tridentView().put(entityId, newData);
        }
        newPackedItems.addAll(newData);
        return newPackedItems;
    }
}
