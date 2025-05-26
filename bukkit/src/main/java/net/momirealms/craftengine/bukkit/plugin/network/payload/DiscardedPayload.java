package net.momirealms.craftengine.bukkit.plugin.network.payload;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.Key;

import java.lang.reflect.InvocationTargetException;

public record DiscardedPayload(Key id, byte[] data) {
    public static final boolean useNewMethod = Reflections.method$DiscardedPayload$data == null;

    public static DiscardedPayload decode(Object payload) throws InvocationTargetException, IllegalAccessException {
        Object type = Reflections.method$CustomPacketPayload$type.invoke(payload);
        Object id = Reflections.method$CustomPacketPayload$Type$id.invoke(type);
        Key channel = Key.of(id.toString());
        byte[] data;
        if (useNewMethod) {
            data = (byte[]) Reflections.method$DiscardedPayload$dataByteArray.invoke(payload);
        } else {
            ByteBuf buf = (ByteBuf) Reflections.method$DiscardedPayload$data.invoke(payload);
            data = new byte[buf.readableBytes()];
            buf.readBytes(data);
        }
        return new DiscardedPayload(channel, data);
    }

    public FriendlyByteBuf toBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(this.data()));
    }
}
