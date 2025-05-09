package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.network.NMSPacketEvent;
import net.momirealms.craftengine.core.item.Enchantment;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MCUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.NamespacedKey;
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

public class CustomTridentUtils {
    public static final NamespacedKey customTridentKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:custom_trident"));
    public static final NamespacedKey interpolationDelayKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:interpolation_delay"));
    public static final NamespacedKey transformationInterpolationDurationaKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:transformation_interpolation_duration"));
    public static final NamespacedKey positionRotationInterpolationDurationKey = Objects.requireNonNull(NamespacedKey.fromString("craftengine:position_rotation_interpolation_duration"));

    public static void handleCustomTrident(NetWorkUser user, NMSPacketEvent event, Object packet) throws IllegalAccessException {
        int entityId = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$entityId(packet);
        Trident trident = getTridentById(user, entityId);
        if (notCustomTrident(trident)) return;
        user.tridentView().put(entityId, List.of());
        modifyCustomTridentPacket(packet);
        user.addTridentPacketView().put(entityId, packet);
        List<Object> itemDisplayValues = buildEntityDataValues(trident);
        user.tridentView().put(entityId, itemDisplayValues);
        user.sendPacket(packet, true);
        user.sendPacket(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityId, itemDisplayValues), true);
        event.setCancelled(true);
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
        String customTrident = container.get(customTridentKey, PersistentDataType.STRING);
        Item<ItemStack> item = BukkitItemManager.instance().createWrappedItem(Key.of(customTrident), null);
        itemStack.getEnchantments().forEach((enchantment, level) -> item.addEnchantment(new Enchantment(Key.of(enchantment.getKey().toString()), level)));
        Integer interpolationDelay = container.get(interpolationDelayKey, PersistentDataType.INTEGER);
        Integer transformationInterpolationDuration = container.get(transformationInterpolationDurationaKey, PersistentDataType.INTEGER);
        Integer positionRotationInterpolationDuration = container.get(positionRotationInterpolationDurationKey, PersistentDataType.INTEGER);
        ItemDisplayEntityData.InterpolationDelay.addEntityDataIfNotDefaultValue(interpolationDelay, itemDisplayValues);
        ItemDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(new Vector3f(0, 0, -2), itemDisplayValues);
        ItemDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(new Quaternionf(1, 1, 1, 1), itemDisplayValues);
        if (VersionHelper.isOrAbove1_20_2()) {
            ItemDisplayEntityData.TransformationInterpolationDuration.addEntityDataIfNotDefaultValue(transformationInterpolationDuration, itemDisplayValues);
            ItemDisplayEntityData.PositionRotationInterpolationDuration.addEntityDataIfNotDefaultValue(positionRotationInterpolationDuration, itemDisplayValues);
        } else {
            ItemDisplayEntityData.InterpolationDuration.addEntityDataIfNotDefaultValue(transformationInterpolationDuration, itemDisplayValues);
        }
        ItemDisplayEntityData.DisplayedItem.addEntityDataIfNotDefaultValue(item.getLiteralObject(), itemDisplayValues);
        ItemDisplayEntityData.DisplayType.addEntityDataIfNotDefaultValue((byte) 0, itemDisplayValues);
        return itemDisplayValues;
    }

    // 这里需要补 ClientboundMoveEntityPacket 包 1.21.2+
    public static void modifyCustomTridentPositionSync(NetWorkUser user, NMSPacketEvent event, Object packet, int entityId) throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Object positionMoveRotation = Reflections.field$ClientboundEntityPositionSyncPacket$values.get(packet);
        boolean onGround = Reflections.field$ClientboundEntityPositionSyncPacket$onGround.getBoolean(packet);
        Object position = Reflections.field$PositionMoveRotation$position.get(positionMoveRotation);
        Object deltaMovement = Reflections.field$PositionMoveRotation$deltaMovement.get(positionMoveRotation);
        float yRot = Reflections.field$PositionMoveRotation$yRot.getFloat(positionMoveRotation);
        float xRot = Reflections.field$PositionMoveRotation$xRot.getFloat(positionMoveRotation);
        Object newPositionMoveRotation = Reflections.constructor$PositionMoveRotation.newInstance(position, deltaMovement, -yRot, Math.clamp(-xRot, -90.0F, 90.0F));
        event.replacePacket(Reflections.constructor$ClientboundEntityPositionSyncPacket.newInstance(entityId, newPositionMoveRotation, onGround));
        // List<SmoothMovementPathUtils.Vector3> path = SmoothMovementPathUtils.calculatePath(start, move, yRot, xRot);
        // ((Player)user.platformPlayer()).sendMessage("entityId: " + entityId + " position: " + position + " deltaMovement: " + deltaMovement + " xRot: " + xRot + " yRot: " + yRot);
    }

    public static void modifyCustomTridentMove(Object packet, NetWorkUser user) throws IllegalAccessException {
        // int entityId = BukkitInjector.internalFieldAccessor().field$ClientboundMoveEntityPacket$entityId(packet);
        // double xa = Reflections.field$ClientboundMoveEntityPacket$xa.getShort(packet);
        // double ya = Reflections.field$ClientboundMoveEntityPacket$ya.getShort(packet);
        // double za = Reflections.field$ClientboundMoveEntityPacket$za.getShort(packet);
        float xRot = MCUtils.unpackDegrees(Reflections.field$ClientboundMoveEntityPacket$xRot.getByte(packet));
        float yRot = MCUtils.unpackDegrees(Reflections.field$ClientboundMoveEntityPacket$yRot.getByte(packet));
        // ((Player)user.platformPlayer()).sendMessage("entityId: " + entityId + " xa: " + xa + " ya: " + ya + " za: " + za + " xRot: " + xRot + " yRot: " + yRot);
        Reflections.field$ClientboundMoveEntityPacket$xRot.setByte(packet, MCUtils.packDegrees(Math.clamp(-xRot, -90.0F, 90.0F)));
        Reflections.field$ClientboundMoveEntityPacket$yRot.setByte(packet, MCUtils.packDegrees(-yRot));
    }

    public static Object buildCustomTridentSetEntityDataPacket(NetWorkUser user, int entityId) {
        List<Object> newData = user.tridentView().getOrDefault(entityId, List.of());
        if (newData.isEmpty()) {
            Trident trident = getTridentById(user, entityId);
            if (notCustomTrident(trident)) return null;
            newData = buildEntityDataValues(trident);
            user.tridentView().put(entityId, newData);
        }
        return FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityId, newData);
    }

    public static void modifyCustomTridentSetEntityData(NetWorkUser user, NMSPacketEvent event, int entityId) {
        if (user.tridentView().containsKey(entityId)) {
            Object packet = buildCustomTridentSetEntityDataPacket(user, entityId);
            if (packet == null) return;
            event.replacePacket(packet);
        } else {
            Trident trident = getTridentById(user, entityId);
            if (trident == null) return;
            if (notCustomTrident(trident)) return;
            Object packet = buildCustomTridentSetEntityDataPacket(user, entityId);
            if (packet == null) return;
            event.replacePacket(packet);
        }
    }
}
