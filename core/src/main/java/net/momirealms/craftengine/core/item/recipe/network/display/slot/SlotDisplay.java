package net.momirealms.craftengine.core.item.recipe.network.display.slot;

import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.function.Function;

public interface SlotDisplay {

    void write(FriendlyByteBuf buf);

    static SlotDisplay read(FriendlyByteBuf buf) {
        return buf.readById(BuiltInRegistries.SLOT_DISPLAY_TYPE).read(buf);
    }

    record Type(Function<FriendlyByteBuf, SlotDisplay> reader) {

        public SlotDisplay read(final FriendlyByteBuf buf) {
            return this.reader.apply(buf);
        }
    }
}
