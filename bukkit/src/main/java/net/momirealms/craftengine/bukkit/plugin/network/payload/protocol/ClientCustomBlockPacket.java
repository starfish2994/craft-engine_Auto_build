package net.momirealms.craftengine.bukkit.plugin.network.payload.protocol;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.plugin.network.ModPacket;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.plugin.network.codec.NetworkCodec;
import net.momirealms.craftengine.core.registry.BuiltInRegistries;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.IntIdentityList;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.ResourceKey;

public record ClientCustomBlockPacket(int size) implements ModPacket {
    public static final ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> TYPE = ResourceKey.create(
            BuiltInRegistries.MOD_PACKET.key().location(), Key.of("craftengine", "client_custom_block")
    );
    public static final NetworkCodec<FriendlyByteBuf, ClientCustomBlockPacket> CODEC = ModPacket.codec(
            ClientCustomBlockPacket::encode,
            ClientCustomBlockPacket::new
    );

    private ClientCustomBlockPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.size);
    }

    @Override
    public ResourceKey<NetworkCodec<FriendlyByteBuf, ? extends ModPacket>> type() {
        return TYPE;
    }

    @Override
    public void handle(NetWorkUser user) {
        int serverBlockRegistrySize = RegistryUtils.currentBlockRegistrySize();
        if (this.size != serverBlockRegistrySize) {
            user.kick(Component.translatable(
                    "disconnect.craftengine.block_registry_mismatch",
                    TranslationArgument.numeric(this.size),
                    TranslationArgument.numeric(serverBlockRegistrySize)
            ));
            return;
        }
        user.setClientModState(true);
        user.setClientBlockList(new IntIdentityList(this.size));
    }

}
