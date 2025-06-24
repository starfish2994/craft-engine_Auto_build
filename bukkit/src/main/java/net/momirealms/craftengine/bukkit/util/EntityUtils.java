package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.core.entity.EquipmentSlot;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.world.BlockPos;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.function.Consumer;

import static net.momirealms.craftengine.core.entity.EquipmentSlot.BODY;
import static net.momirealms.craftengine.core.entity.EquipmentSlot.MAIN_HAND;

public class EntityUtils {

    private EntityUtils() {}

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

    public static org.bukkit.inventory.EquipmentSlot toBukkitEquipmentSlot(EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> org.bukkit.inventory.EquipmentSlot.HAND;
            case OFF_HAND -> org.bukkit.inventory.EquipmentSlot.OFF_HAND;
            case HEAD -> org.bukkit.inventory.EquipmentSlot.HEAD;
            case CHEST -> org.bukkit.inventory.EquipmentSlot.CHEST;
            case LEGS -> org.bukkit.inventory.EquipmentSlot.LEGS;
            case FEET -> org.bukkit.inventory.EquipmentSlot.FEET;
            default -> org.bukkit.inventory.EquipmentSlot.BODY;
        };
    }

    public static EquipmentSlot toCEEquipmentSlot(org.bukkit.inventory.EquipmentSlot slot) {
        return switch (slot) {
            case HAND -> MAIN_HAND;
            case OFF_HAND -> EquipmentSlot.OFF_HAND;
            case HEAD -> EquipmentSlot.HEAD;
            case CHEST -> EquipmentSlot.CHEST;
            case LEGS -> EquipmentSlot.LEGS;
            case FEET -> EquipmentSlot.FEET;
            case BODY -> EquipmentSlot.BODY;
            default -> BODY;
        };
    }

    public static Object fromEquipmentSlot(EquipmentSlot slot) {
        return switch (slot) {
            case MAIN_HAND -> CoreReflections.instance$EquipmentSlot$MAINHAND;
            case OFF_HAND -> CoreReflections.instance$EquipmentSlot$OFFHAND;
            case HEAD -> CoreReflections.instance$EquipmentSlot$HEAD;
            case CHEST -> CoreReflections.instance$EquipmentSlot$CHEST;
            case LEGS -> CoreReflections.instance$EquipmentSlot$LEGS;
            case FEET -> CoreReflections.instance$EquipmentSlot$FEET;
            default -> new Object();
        };
    }
}
