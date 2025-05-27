package net.momirealms.craftengine.bukkit.item;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.item.NetworkItemHandler;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class LegacyNetworkItemHandler implements NetworkItemHandler<ItemStack> {
    private final BukkitItemManager itemManager;

    public LegacyNetworkItemHandler(BukkitItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public Optional<Item<ItemStack>> c2s(Item<ItemStack> itemStack, ItemBuildContext context) {
        return Optional.empty();
    }

    @Override
    public Optional<Item<ItemStack>> s2c(Item<ItemStack> itemStack, ItemBuildContext context) {
        return Optional.empty();
    }
}
