package net.momirealms.craftengine.bukkit.plugin.network.payload.codec;

@FunctionalInterface
public interface NetworkMemberEncoder<O, T> {
    void encode(T object, O object2);
}
