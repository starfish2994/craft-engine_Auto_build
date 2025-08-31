package net.momirealms.craftengine.bukkit.plugin.network.payload;

import io.netty.buffer.ByteBuf;
import net.momirealms.craftengine.bukkit.plugin.network.payload.codec.NetworkCodec;
import net.momirealms.craftengine.bukkit.plugin.network.payload.codec.NetworkDecoder;
import net.momirealms.craftengine.bukkit.plugin.network.payload.codec.NetworkMemberEncoder;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;

public interface Data {

    default void handle(NetWorkUser user) {
    }

    static <B extends ByteBuf, T extends Data> NetworkCodec<B, T> codec(NetworkMemberEncoder<B, T> networkMemberEncoder, NetworkDecoder<B, T> networkDecoder) {
        return NetworkCodec.ofMember(networkMemberEncoder, networkDecoder);
    }

}
