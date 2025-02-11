package net.momirealms.craftengine.core.item.modifier;

import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;

public interface ItemModifier<I> {

    String name();

    void apply(Item<I> item, Player player);
}
