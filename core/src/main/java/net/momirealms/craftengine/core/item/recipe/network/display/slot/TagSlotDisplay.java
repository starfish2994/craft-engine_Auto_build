package net.momirealms.craftengine.core.item.recipe.network.display.slot;

import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

public class TagSlotDisplay implements SlotDisplay {
    private final Key tag;

    public TagSlotDisplay(Key tag) {
        this.tag = tag;
    }

    public static TagSlotDisplay read(FriendlyByteBuf buf) {
        return new TagSlotDisplay(buf.readKey());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeKey(this.tag);
    }
}
