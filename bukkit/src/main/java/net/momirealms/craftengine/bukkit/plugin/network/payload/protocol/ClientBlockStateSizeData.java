package net.momirealms.craftengine.bukkit.plugin.network.payload.protocol;


import net.momirealms.craftengine.bukkit.plugin.network.payload.Data;
import net.momirealms.craftengine.bukkit.plugin.network.payload.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;
import net.momirealms.craftengine.core.util.IntIdentityList;

public record ClientBlockStateSizeData(int blockStateSize) implements Data {
    public static final NetworkCodec<FriendlyByteBuf, ClientBlockStateSizeData> CODEC = Data.codec(
            ClientBlockStateSizeData::encode,
            ClientBlockStateSizeData::new
    );

    private ClientBlockStateSizeData(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.blockStateSize);
    }

    @Override
    public void handle(NetWorkUser user) {
        user.setClientBlockList(new IntIdentityList(this.blockStateSize));
    }

}
