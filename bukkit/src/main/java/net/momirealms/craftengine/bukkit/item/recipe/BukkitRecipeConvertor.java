package net.momirealms.craftengine.bukkit.item.recipe;

import net.momirealms.craftengine.core.item.recipe.Recipe;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.inventory.ItemStack;

public interface BukkitRecipeConvertor<T extends Recipe<ItemStack>> {

    Runnable convert(Key id, T recipe);
}
