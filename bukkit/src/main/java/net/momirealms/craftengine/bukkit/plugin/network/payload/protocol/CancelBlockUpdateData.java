package net.momirealms.craftengine.bukkit.plugin.network.payload.protocol;

import net.momirealms.craftengine.bukkit.plugin.network.payload.Data;
import net.momirealms.craftengine.bukkit.plugin.network.payload.PayloadHelper;
import net.momirealms.craftengine.bukkit.plugin.network.payload.codec.NetworkCodec;
import net.momirealms.craftengine.core.plugin.network.NetWorkUser;
import net.momirealms.craftengine.core.util.FriendlyByteBuf;

public record CancelBlockUpdateData(boolean enabled) implements Data {
    public static final NetworkCodec<FriendlyByteBuf, CancelBlockUpdateData> CODEC = Data.codec(
            CancelBlockUpdateData::encode,
            CancelBlockUpdateData::new
    );

    private CancelBlockUpdateData(FriendlyByteBuf buf) {
        this(buf.readBoolean());
    }

    private void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.enabled);
    }

    @Override
    public void handle(NetWorkUser user) {
        if (!this.enabled) return;
        PayloadHelper.sendData(user, this);
    }
}
