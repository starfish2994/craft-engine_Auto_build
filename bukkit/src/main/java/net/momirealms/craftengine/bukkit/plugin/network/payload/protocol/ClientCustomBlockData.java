package net.momirealms.craftengine.bukkit.plugin.network.payload.protocol;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.momirealms.craftengine.bukkit.plugin.network.payload.Data;
import net.momirealms.craftengine.bukkit.plugin.network.payload.codec.NetworkCodec;
import net.momirealms.craftengine.bukkit.util.RegistryUtils;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.IntIdentityList;

public record ClientCustomBlockData(int size) implements Data {
    public static final NetworkCodec<FriendlyByteBuf, ClientCustomBlockData> CODEC = Data.codec(
            ClientCustomBlockData::encode,
            ClientCustomBlockData::new
    );

    private ClientCustomBlockData(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.size);
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
