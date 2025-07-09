package net.momirealms.craftengine.core.item.recipe.network.display.slot;

import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public class ItemStackSlotDisplay implements SlotDisplay {
    private final Item<?> item;

    public ItemStackSlotDisplay(Item<?> item) {
        this.item = item;
    }

    public static ItemStackSlotDisplay read(FriendlyByteBuf buf) {
        Item<?> itemStack = CraftEngine.instance().itemManager().decode(buf);
        return new ItemStackSlotDisplay(itemStack);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void write(FriendlyByteBuf buf) {
        CraftEngine.instance().itemManager().encode(buf, (Item<Object>) this.item);
    }

    public Item<?> item() {
        return item;
    }
}
