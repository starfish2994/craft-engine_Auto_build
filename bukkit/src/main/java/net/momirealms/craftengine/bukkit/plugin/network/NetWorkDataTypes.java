package net.momirealms.craftengine.bukkit.plugin.network;


import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum NetWorkDataTypes {
    CLIENT_CUSTOM_BLOCK(
            FriendlyByteBuf::readInt,
            FriendlyByteBuf::writeInt
    ),
    CANCEL_BLOCK_UPDATE(
            FriendlyByteBuf::readBoolean,
            FriendlyByteBuf::writeBoolean
    );

    private final Function<FriendlyByteBuf, ?> decoder;
    private final BiConsumer<FriendlyByteBuf, Object> encoder;

    @SuppressWarnings("unchecked")
    <T> NetWorkDataTypes(Function<FriendlyByteBuf, T> decoder, BiConsumer<FriendlyByteBuf, T> encoder) {
        this.decoder = decoder;
        this.encoder = (buf, data) -> encoder.accept(buf, (T) data);
    }

    @SuppressWarnings("unchecked")
    public <T> T decode(FriendlyByteBuf buf) {
        return (T) decoder.apply(buf);
    }

    public <T> void encode(FriendlyByteBuf buf, T data) {
        encoder.accept(buf, data);
    }
}
