package net.momirealms.craftengine.bukkit.item;

import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public interface NetworkItemHandler {

    Optional<ItemStack> s2c(ItemStack itemStack, ItemBuildContext context);

    Optional<ItemStack> c2s(ItemStack itemStack, ItemBuildContext context);
}
