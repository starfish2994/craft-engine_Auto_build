package net.momirealms.craftengine.bukkit.entity.furniture;

import net.momirealms.craftengine.bukkit.entity.data.ItemDisplayEntityData;
import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.entity.furniture.AbstractFurnitureElement;
import net.momirealms.craftengine.core.entity.furniture.Billboard;
import net.momirealms.craftengine.core.entity.furniture.ItemDisplayContext;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.world.World;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class BukkitFurnitureElement extends AbstractFurnitureElement {
    private List<Object> cachedValues;

    public BukkitFurnitureElement(Key item,
                                  Billboard billboard,
                                  ItemDisplayContext transform,
                                  Vector3f scale,
                                  Vector3f translation,
                                  Vector3f offset,
                                  Quaternionf rotation) {
        super(item, billboard, transform, scale, translation, offset, rotation);
    }

    @Override
    public void initPackets(int entityId, World world, double x, double y, double z, float yaw, Quaternionf conjugated, Consumer<Object> packets) {
        Vector3f offset = conjugated.transform(new Vector3f(position()));
        packets.accept(FastNMS.INSTANCE.constructor$ClientboundAddEntityPacket(
                entityId, UUID.randomUUID(), x + offset.x, y + offset.y, z - offset.z, 0, yaw,
                Reflections.instance$EntityType$ITEM_DISPLAY, 0, Reflections.instance$Vec3$Zero, 0
        ));
        packets.accept(FastNMS.INSTANCE.constructor$ClientboundSetEntityDataPacket(entityId, getCachedValues()));
    }

    private synchronized List<Object> getCachedValues() {
        if (this.cachedValues == null) {
            this.cachedValues = new ArrayList<>();
            Item<ItemStack> item = BukkitItemManager.instance().createWrappedItem(item(), null);
            if (item == null) {
                CraftEngine.instance().logger().warn("Failed to create furniture element for " + item() + " because item " + item() + " not found");
                item = BukkitItemManager.instance().wrap(new ItemStack(Material.STONE));
            }
            ItemDisplayEntityData.DisplayedItem.addEntityDataIfNotDefaultValue(item.getLiteralObject(), this.cachedValues);
            ItemDisplayEntityData.Scale.addEntityDataIfNotDefaultValue(scale(), this.cachedValues);
            ItemDisplayEntityData.RotationLeft.addEntityDataIfNotDefaultValue(rotation(), this.cachedValues);
            ItemDisplayEntityData.BillboardConstraints.addEntityDataIfNotDefaultValue(billboard().id(), this.cachedValues);
            ItemDisplayEntityData.Translation.addEntityDataIfNotDefaultValue(translation(), this.cachedValues);
            ItemDisplayEntityData.DisplayType.addEntityDataIfNotDefaultValue(transform().id(), this.cachedValues);
        }
        return this.cachedValues;
    }
}
