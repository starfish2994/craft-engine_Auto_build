package net.momirealms.craftengine.core.item.recipe;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.BuildableItem;

public record CustomRecipeResult<T>(BuildableItem<T> item, int count) {

    public T buildItemStack(Player player) {
        return item.buildItemStack(player, count);
    }
}
