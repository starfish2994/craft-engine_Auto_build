package net.momirealms.craftengine.fabric.client.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record CraftEnginePayload(byte[] data) implements CustomPayload {
    public static final Identifier ADD_CRAFTENGINE_BLOCK = Identifier.of("craftengine", "payload");
    public static final Id<CraftEnginePayload> ID = new Id<>(CraftEnginePayload.ADD_CRAFTENGINE_BLOCK);
    public static final PacketCodec<RegistryByteBuf, CraftEnginePayload> CODEC = PacketCodec.tuple(
            new ByteArrayCodec(), CraftEnginePayload::data,
            CraftEnginePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
