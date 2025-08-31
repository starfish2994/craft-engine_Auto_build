package net.momirealms.craftengine.bukkit.plugin.network.payload;

import io.netty.buffer.Unpooled;
import net.momirealms.craftengine.bukkit.plugin.network.payload.codec.NetworkCodec;
import net.momirealms.craftengine.bukkit.plugin.network.payload.protocol.CancelBlockUpdateData;
import net.momirealms.craftengine.bukkit.plugin.network.payload.protocol.ClientBlockStateSizeData;
import net.momirealms.craftengine.bukkit.plugin.network.payload.protocol.ClientCustomBlockData;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.NetworkManager;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class PayloadHelper {
    private static final Map<Class<Data>, Byte> classToType = new HashMap<>();
    private static final Map<Byte, NetworkCodec<FriendlyByteBuf, Data>> typeToCodec = new HashMap<>();
    private static final AtomicInteger typeCounter = new AtomicInteger(0);

    public static void registerDataTypes() {
        registerDataType(ClientCustomBlockData.class, ClientCustomBlockData.CODEC);
        registerDataType(CancelBlockUpdateData.class, CancelBlockUpdateData.CODEC);
        registerDataType(ClientBlockStateSizeData.class, ClientBlockStateSizeData.CODEC);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Data> void registerDataType(Class<T> dataClass, NetworkCodec<FriendlyByteBuf, T> codec) {
        if (classToType.containsKey(dataClass)) {
            CraftEngine.instance().logger().warn("Duplicate data type class: " + dataClass.getName());
            return;
        }
        int next = typeCounter.getAndIncrement();
        if (next > 255) {
            throw new IllegalStateException("Too many data types registered, byte index overflow (max 256)");
        }
        byte type = (byte) next;
        classToType.put((Class<Data>) dataClass, type);
        typeToCodec.put(type, (NetworkCodec<FriendlyByteBuf, Data>) codec);
    }

    public static void sendData(NetWorkUser user, Data data) {
        Class<? extends Data> dataClass = data.getClass();
        Byte type = classToType.get(dataClass);
        if (type == null) {
            CraftEngine.instance().logger().warn("Unknown data type class: " + dataClass.getName());
            return;
        }
        NetworkCodec<FriendlyByteBuf, Data> codec = typeToCodec.get(type);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeByte(type);
        codec.encode(buf, data);
        user.sendCustomPayload(NetworkManager.MOD_CHANNEL_KEY, buf.array());
    }

    public static void handleReceiver(Payload payload, NetWorkUser user) {
        FriendlyByteBuf buf = payload.toBuffer();
        byte type = buf.readByte();
        NetworkCodec<FriendlyByteBuf, Data> codec = typeToCodec.get(type);
        if (codec == null) {
            CraftEngine.instance().logger().warn("Unknown data type received: " + type);
            return;
        }

        Data networkData = codec.decode(buf);
        networkData.handle(user);
    }
}
