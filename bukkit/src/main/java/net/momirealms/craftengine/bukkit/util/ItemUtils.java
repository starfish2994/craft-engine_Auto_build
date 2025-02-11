package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.VersionHelper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.Contract;

import java.lang.reflect.InvocationTargetException;

public class ItemUtils {

    @Contract("null -> true")
    public static boolean isEmpty(final ItemStack item) {
        if (item == null) return true;
        if (item.getType() == Material.AIR) return true;
        return item.getAmount() == 0;
    }

    public static void setItem(PlayerInventory inventory, int slot, ItemStack itemStack) {
        try {
            Object nmsInventory$getInventory = Reflections.clazz$CraftInventoryPlayer
                    .getMethod("getInventory").invoke(inventory);
            Object nmsInventory$items = Reflections.clazz$Inventory
                    .getDeclaredField(VersionHelper.isMojmap() ? "items" : "i")
                    .get(nmsInventory$getInventory);
            Object nmsItemStack = Reflections.clazz$CraftItemStack
                    .getMethod("asNMSCopy", ItemStack.class)
                    .invoke(null, itemStack);
            nmsInventory$items.getClass()
                    .getMethod("set", int.class, Object.class)
                    .invoke(nmsInventory$items, slot, nmsItemStack);
        } catch (InvocationTargetException | IllegalAccessException |
                 NoSuchMethodException | NoSuchFieldException e) {
            CraftEngine.instance().logger().warn("Failed to set item", e);
        }
    }
}
