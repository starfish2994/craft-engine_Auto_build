package net.momirealms.craftengine.core.item;

import net.momirealms.craftengine.core.entity.player.Player;

public class ConstantItem<I> implements BuildableItem<I> {
    private final I item;

    public ConstantItem(I item) {
        this.item = item;
    }

    @Override
    public I buildItemStack(Player player, int count) {
        return item;
    }
}
