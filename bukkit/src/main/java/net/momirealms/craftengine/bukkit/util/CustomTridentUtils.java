package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.network.NMSPacketEvent;
import net.momirealms.craftengine.core.entity.CustomTrident;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Enchantment;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.scheduler.SchedulerTask;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MCUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CustomTridentUtils {

    public static void handleCustomTrident(NetWorkUser user, NMSPacketEvent event, Object packet) {
        int entityId = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$entityId(packet);
        Trident trident = getTridentById(user, entityId);
        if (trident == null) return;
        World world = trident.getWorld();
        Object nmsEntity = FastNMS.INSTANCE.method$CraftEntity$getHandle(trident);
        Object trackedEntity = FastNMS.INSTANCE.field$Entity$trackedEntity(nmsEntity);
        Object serverEntity = FastNMS.INSTANCE.filed$ChunkMap$TrackedEntity$serverEntity(trackedEntity);
        if (notCustomTrident(trident)) return;
        Object newPacket = modifyCustomTridentPacket(packet, entityId);
        List<Object> itemDisplayValues = buildEntityDataValues(trident);
        user.tridentView().put(entityId, itemDisplayValues);
        user.sendPacket(newPacket, true);
        user.sendPacket(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityId, itemDisplayValues), true);
        event.setCancelled(true);
        if (serverEntity != null) {
            // 这里直接暴力更新
            SchedulerTask task = CraftEngine.instance().scheduler().asyncRepeating(() -> {
                FastNMS.INSTANCE.method$ServerEntity$sendChanges(serverEntity);
                if (canSpawnParticle(nmsEntity)) {
                    world.spawnParticle(ParticleUtils.getParticle("BUBBLE"), trident.getLocation(), 1, 0, 0, 0, 0);
                }
            }, 0, 5, TimeUnit.MILLISECONDS);
            user.tridentTaskView().put(entityId, task);
        }
    }

    private static boolean canSpawnParticle(Object nmsEntity) {
        if (!FastNMS.INSTANCE.field$AbstractArrow$wasTouchingWater(nmsEntity)) return false;
        return !FastNMS.INSTANCE.method$AbstractArrow$isInGround(nmsEntity);
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
        Optional<CustomItem<ItemStack>> customItem = BukkitItemManager.instance().wrap(trident.getItemStack()).getCustomItem();
        return customItem.map(itemStackCustomItem -> itemStackCustomItem.settings().customTrident() == null).orElse(true);
    }

    public static Object modifyCustomTridentPacket(Object packet, int entityId) {
        UUID uuid = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$uuid(packet);
        double x = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$x(packet);
        double y = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$y(packet);
        double z = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$z(packet);
        float yRot = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$yRot(packet);
        float xRot = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$xRot(packet);
        Object type = Reflections.instance$EntityType$ITEM_DISPLAY;
        int data = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$data(packet);
        double xa = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$xa(packet);
        double ya = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$ya(packet);
        double za = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$za(packet);
        double yHeadRot = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$yHeadRot(packet);
        return FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                entityId, uuid, x, y, z,
                MCUtils.clamp(-xRot, -90.0F, 90.0F), -yRot,
                type, data, FastNMS.INSTANCE.constructor$Vec3(xa, ya, za), yHeadRot
        );
    }

    public static List<Object> buildEntityDataValues(Trident trident) {
        List<Object> itemDisplayValues = new ArrayList<>();
        ItemStack itemStack = trident.getItemStack();
        Optional<CustomItem<ItemStack>> customItem = BukkitItemManager.instance().wrap(itemStack).getCustomItem();
        if (customItem.isEmpty()) return itemDisplayValues;
        CustomTrident customTrident = customItem.get().settings().customTrident();
        Item<ItemStack> item = BukkitItemManager.instance().createWrappedItem(customTrident.customTridentItemId(), null);
        itemStack.getEnchantments().forEach((enchantment, level) -> item.addEnchantment(new Enchantment(Key.of(enchantment.getKey().toString()), level)));
        ItemDisplayEntityData.InterpolationDelay.addEntityDataIfNotDefaultValue(-1, itemDisplayValues);
        ItemDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(customTrident.translation(), itemDisplayValues);
        ItemDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(customTrident.rotationLefts(), itemDisplayValues);
        if (VersionHelper.isOrAbove1_20_2()) {
            ItemDisplayEntityData.TransformationInterpolationDuration.addEntityDataIfNotDefaultValue(2, itemDisplayValues);
            ItemDisplayEntityData.PositionRotationInterpolationDuration.addEntityDataIfNotDefaultValue(2, itemDisplayValues);
        } else {
            ItemDisplayEntityData.InterpolationDuration.addEntityDataIfNotDefaultValue(2, itemDisplayValues);
        }
        ItemDisplayEntityData.DisplayedItem.addEntityDataIfNotDefaultValue(item.getLiteralObject(), itemDisplayValues);
        ItemDisplayEntityData.DisplayType.addEntityDataIfNotDefaultValue(customTrident.displayType(), itemDisplayValues);
        return itemDisplayValues;
    }

    public static Object buildCustomTridentPositionSync(Object packet, int entityId) {
        Object positionMoveRotation = FastNMS.INSTANCE.field$ClientboundEntityPositionSyncPacket$values(packet);
        boolean onGround = FastNMS.INSTANCE.field$ClientboundEntityPositionSyncPacket$onGround(packet);
        Object position = FastNMS.INSTANCE.field$PositionMoveRotation$position(positionMoveRotation);
        Object deltaMovement = FastNMS.INSTANCE.field$PositionMoveRotation$deltaMovement(positionMoveRotation);
        float yRot = FastNMS.INSTANCE.field$PositionMoveRotation$yRot(positionMoveRotation);
        float xRot = FastNMS.INSTANCE.field$PositionMoveRotation$xRot(positionMoveRotation);
        Object newPositionMoveRotation = FastNMS.INSTANCE.constructor$PositionMoveRotation(position, deltaMovement, -yRot, Math.clamp(-xRot, -90.0F, 90.0F));
        return FastNMS.INSTANCE.constructor$ClientboundEntityPositionSyncPacket(entityId, newPositionMoveRotation, onGround);
    }

    public static Object buildCustomTridentMove(Object packet, int entityId) {
        short xa = FastNMS.INSTANCE.field$ClientboundMoveEntityPacket$xa(packet);
        short ya = FastNMS.INSTANCE.field$ClientboundMoveEntityPacket$ya(packet);
        short za = FastNMS.INSTANCE.field$ClientboundMoveEntityPacket$za(packet);
        float xRot = MCUtils.unpackDegrees(FastNMS.INSTANCE.field$ClientboundMoveEntityPacket$xRot(packet));
        float yRot = MCUtils.unpackDegrees(FastNMS.INSTANCE.field$ClientboundMoveEntityPacket$yRot(packet));
        boolean onGround = FastNMS.INSTANCE.field$ClientboundMoveEntityPacket$onGround(packet);
        return FastNMS.INSTANCE.constructor$ClientboundMoveEntityPacket$PosRot(
                entityId, xa, ya, za,
                MCUtils.packDegrees(-yRot), MCUtils.packDegrees(MCUtils.clamp(-xRot, -90.0F, 90.0F)),
                onGround
        );
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
