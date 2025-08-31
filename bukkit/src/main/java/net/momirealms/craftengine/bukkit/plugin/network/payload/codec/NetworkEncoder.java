package net.momirealms.craftengine.bukkit.plugin.network.payload.codec;

public interface NetworkEncoder<O, T> {
    void encode(O out, T value);
}
