package net.momirealms.craftengine.bukkit.util;

import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;

public class LegacyInventoryUtils {

    public static Inventory getTopInventory(Player player) {
        return player.getOpenInventory().getTopInventory();
    }

    public static void setRepairCost(AnvilInventory anvilInventory, int repairCost, int amount) {
        anvilInventory.setRepairCost(repairCost);
        anvilInventory.setRepairCostAmount(amount);
    }
}
