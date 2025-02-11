package net.momirealms.craftengine.bukkit.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;

public class ItemUtils {

    @Contract("null -> true")
    public static boolean isEmpty(final ItemStack item) {
        if (item == null) return true;
        if (item.getType() == Material.AIR) return true;
        return item.getAmount() == 0;
    }
}
