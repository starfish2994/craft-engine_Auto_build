package net.momirealms.craftengine.bukkit.item;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.BuildableItem;
import org.bukkit.inventory.ItemStack;

public class CloneableConstantItem implements BuildableItem<ItemStack> {
    private final ItemStack item;

    public CloneableConstantItem(ItemStack item) {
        this.item = item;
    }

    @Override
    public ItemStack buildItemStack(Player player, int count) {
        ItemStack itemStack = this.item.clone();
        itemStack.setAmount(count);
        return itemStack;
    }
}
