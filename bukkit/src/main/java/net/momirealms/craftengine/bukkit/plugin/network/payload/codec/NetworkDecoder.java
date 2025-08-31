package net.momirealms.craftengine.bukkit.plugin.network.payload.codec;

public interface NetworkDecoder<I, T> {
    T decode(I in);
}
