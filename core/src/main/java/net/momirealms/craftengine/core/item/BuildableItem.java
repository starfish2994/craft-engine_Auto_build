package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.entity.player.Player;

public interface BuildableItem<I> {

    I buildItemStack(Player player, int count);
}
