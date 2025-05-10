package net.momirealms.craftengine.bukkit.plugin.network.handler;

import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.injector.BukkitInjector;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.entity.projectile.CustomProjectile;
import net.momirealms.craftengine.core.entity.projectile.ProjectileMeta;
import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.network.ByteBufPacketEvent;
import net.momirealms.craftengine.core.plugin.network.EntityPacketHandler;
import net.momirealms.craftengine.core.plugin.network.NMSPacketEvent;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.MCUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProjectilePacketHandler implements EntityPacketHandler {
    private final CustomProjectile projectile;

    public ProjectilePacketHandler(CustomProjectile projectile) {
        this.projectile = projectile;
    }

    @Override
    public void handleSetEntityData(NetWorkUser user, ByteBufPacketEvent event) {
        FriendlyByteBuf buf = event.getBuffer();
        int id = buf.readVarInt();
        List<Object> packedItems = FastNMS.INSTANCE.method$ClientboundSetEntityDataPacket$unpack(buf);
        List<Object> newPackedItems = convertCustomProjectileSetEntityDataPacket(packedItems);
        event.setChanged(true);
        buf.clear();
        buf.writeVarInt(event.packetID());
        buf.writeVarInt(id);
        FastNMS.INSTANCE.method$ClientboundSetEntityDataPacket$pack(newPackedItems, buf);
    }

    @Override
    public void handleSyncEntityPosition(NetWorkUser user, NMSPacketEvent event, Object packet) {
        Object converted = convertCustomProjectilePositionSyncPacket(packet);
        event.replacePacket(converted);
    }

    @Override
    public void handleMoveAndRotate(NetWorkUser user, NMSPacketEvent event, Object packet) {
        Object converted = convertCustomProjectileMovePacket(packet);
        event.replacePacket(converted);
    }

    public static Object convertAddCustomProjectPacket(Object packet) {
        int entityId = FastNMS.INSTANCE.field$ClientboundAddEntityPacket$entityId(packet);
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

    private Object convertCustomProjectilePositionSyncPacket(Object packet) {
        int entityId = FastNMS.INSTANCE.method$ClientboundEntityPositionSyncPacket$id(packet);
        Object positionMoveRotation = FastNMS.INSTANCE.field$ClientboundEntityPositionSyncPacket$values(packet);
        boolean onGround = FastNMS.INSTANCE.field$ClientboundEntityPositionSyncPacket$onGround(packet);
        Object position = FastNMS.INSTANCE.field$PositionMoveRotation$position(positionMoveRotation);
        Object deltaMovement = FastNMS.INSTANCE.field$PositionMoveRotation$deltaMovement(positionMoveRotation);
        float yRot = FastNMS.INSTANCE.field$PositionMoveRotation$yRot(positionMoveRotation);
        float xRot = FastNMS.INSTANCE.field$PositionMoveRotation$xRot(positionMoveRotation);
        Object newPositionMoveRotation = FastNMS.INSTANCE.constructor$PositionMoveRotation(position, deltaMovement, -yRot, Math.clamp(-xRot, -90.0F, 90.0F));
        return FastNMS.INSTANCE.constructor$ClientboundEntityPositionSyncPacket(entityId, newPositionMoveRotation, onGround);
    }

    // 将原有的投掷物的entity data转化为展示实体的数据包
    public List<Object> convertCustomProjectileSetEntityDataPacket(List<Object> packedItems) {
        List<Object> newPackedItems = new ArrayList<>();
        for (Object packedItem : packedItems) {
            int entityDataId = FastNMS.INSTANCE.field$SynchedEntityData$DataValue$id(packedItem);
            if (entityDataId < 8) {
                newPackedItems.add(packedItem);
            }
        }
        newPackedItems.addAll(createCustomProjectileEntityDataValues());
        return newPackedItems;
    }

    private List<Object> createCustomProjectileEntityDataValues() {
        List<Object> itemDisplayValues = new ArrayList<>();
        Optional<CustomItem<ItemStack>> customItem = BukkitItemManager.instance().getCustomItem(this.projectile.metadata().item());
        if (customItem.isEmpty()) return itemDisplayValues;
        ProjectileMeta meta = projectile.metadata();
        Item<?> displayedItem = customItem.get().buildItem(ItemBuildContext.EMPTY);
        // 我们应当使用新的展示物品的组件覆盖原物品的组件，以完成附魔，附魔光效等组件的继承
        displayedItem = this.projectile.item().mergeCopy(displayedItem);
        ItemDisplayEntityData.InterpolationDelay.addEntityDataIfNotDefaultValue(-1, itemDisplayValues);
        ItemDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(meta.translation(), itemDisplayValues);
        ItemDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(meta.rotation(), itemDisplayValues);
        if (VersionHelper.isOrAbove1_20_2()) {
            ItemDisplayEntityData.TransformationInterpolationDuration.addEntityDataIfNotDefaultValue(2, itemDisplayValues);
            ItemDisplayEntityData.PositionRotationInterpolationDuration.addEntityDataIfNotDefaultValue(2, itemDisplayValues);
        } else {
            ItemDisplayEntityData.InterpolationDuration.addEntityDataIfNotDefaultValue(2, itemDisplayValues);
        }
        ItemDisplayEntityData.DisplayedItem.addEntityDataIfNotDefaultValue(displayedItem.getLiteralObject(), itemDisplayValues);
        ItemDisplayEntityData.DisplayType.addEntityDataIfNotDefaultValue(meta.displayType().id(), itemDisplayValues);
        return itemDisplayValues;
    }

    private static Object convertCustomProjectileMovePacket(Object packet) {
        int entityId = BukkitInjector.internalFieldAccessor().field$ClientboundMoveEntityPacket$entityId(packet);
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
}
