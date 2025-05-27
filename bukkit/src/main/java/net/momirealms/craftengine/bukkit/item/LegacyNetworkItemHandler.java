package net.momirealms.craftengine.bukkit.item;

import net.momirealms.craftengine.core.item.CustomItem;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class LegacyNetworkItemHandler implements NetworkItemHandler {
    private final BukkitItemManager itemManager;

    public LegacyNetworkItemHandler(BukkitItemManager itemManager) {
        this.itemManager = itemManager;
    }

    @Override
    public Optional<ItemStack> c2s(ItemStack itemStack, ItemBuildContext context) {
        Item<ItemStack> wrapped = this.itemManager.wrap(itemStack);
        if (wrapped == null) return Optional.empty();

        return Optional.empty();
    }

    @Override
    public Optional<ItemStack> s2c(ItemStack itemStack, ItemBuildContext context) {
        Item<ItemStack> wrapped = this.itemManager.wrap(itemStack);
        if (wrapped == null) return Optional.empty();
        Optional<CustomItem<ItemStack>> optionalCustomItem = wrapped.getCustomItem();
        if (optionalCustomItem.isEmpty()) {
            return s2cOtherItems(wrapped, context);
        } else {
            return Optional.empty();
        }
    }

    private Optional<ItemStack> s2cOtherItems(Item<ItemStack> item, ItemBuildContext context) {
        if (!Config.interceptItem()) return Optional.empty();

//        Optional<List<String>> optionalLore = item.lore();
//        if (optionalLore.isPresent()) {
//            boolean changed = false;
//            List<String> lore = optionalLore.get();
//            List<String> newLore = new ArrayList<>(lore.size());
//            for (String line : lore) {
//                Map<String, Component> tokens = CraftEngine.instance().fontManager().matchTags(line);
//                if (tokens.isEmpty()) {
//                    newLore.add(line);
//                } else {
//                    newLore.add(AdventureHelper.componentToJson(AdventureHelper.replaceText(AdventureHelper.jsonToComponent(line), tokens)));
//                    changed = true;
//                }
//            }
//            if (changed) {
//                item.lore(newLore);
//            }
//        }
//
        return Optional.empty();

    }
}
