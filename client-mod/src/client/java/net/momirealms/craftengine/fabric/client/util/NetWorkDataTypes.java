package net.momirealms.craftengine.fabric.client.util;

import net.minecraft.network.PacketByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public enum NetWorkDataTypes {
    CLIENT_CUSTOM_BLOCK(
            PacketByteBuf::readInt,
            PacketByteBuf::writeInt
    ),
    CANCEL_BLOCK_UPDATE(
            PacketByteBuf::readBoolean,
            PacketByteBuf::writeBoolean
    );

    private final Function<PacketByteBuf, ?> decoder;
    private final BiConsumer<PacketByteBuf, Object> encoder;

    @SuppressWarnings("unchecked")
    <T> NetWorkDataTypes(Function<PacketByteBuf, T> decoder, BiConsumer<PacketByteBuf, T> encoder) {
        this.decoder = decoder;
        this.encoder = (buf, data) -> encoder.accept(buf, (T) data);
    }

    @SuppressWarnings("unchecked")
    public <T> T decode(PacketByteBuf buf) {
        return (T) decoder.apply(buf);
    }

    public <T> void encode(PacketByteBuf buf, T data) {
        encoder.accept(buf, data);
    }
}
