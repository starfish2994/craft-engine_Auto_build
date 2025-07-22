package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.item.BukkitItemManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class EntityUtils {

    private EntityUtils() {
    }

    public static BlockPos getOnPos(Player player) {
        try {
            Object serverPlayer = FastNMS.INSTANCE.method$CraftPlayer$getHandle(player);
            Object blockPos = CoreReflections.method$Entity$getOnPos.invoke(serverPlayer, 1.0E-5F);
            return LocationUtils.fromBlockPos(blockPos);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Entity spawnEntity(World world, Location loc, EntityType type, Consumer<Entity> function) {
        if (VersionHelper.isOrAbove1_20_2()) {
            return world.spawnEntity(loc, type, CreatureSpawnEvent.SpawnReason.CUSTOM, function);
        } else {
            return LegacyEntityUtils.spawnEntity(world, loc, type, function);
        }
    }

    public static boolean isPetOwner(Player player, Entity entity) {
        return entity instanceof Tameable tameable
                && tameable.isTamed()
                && tameable.getOwnerUniqueId() == player.getUniqueId();
    }

    public static boolean isHappyGhastRideable(Entity entity) {
        if (!VersionHelper.isOrAbove1_21_6()) return false;
        if (entity instanceof LivingEntity living && entity.getType() == EntityType.HAPPY_GHAST) {
            ItemStack bodyItem = living.getEquipment().getItem(EquipmentSlot.BODY);
            Item<ItemStack> wrapped = BukkitItemManager.instance().wrap(bodyItem);
            return wrapped.is(Key.of("harnesses"));
        }
        return false;
    }
}