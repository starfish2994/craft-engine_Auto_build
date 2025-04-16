package net.momirealms.craftengine.core.plugin.network;

import io.netty.channel.Channel;
import net.momirealms.craftengine.core.plugin.Plugin;
import net.momirealms.craftengine.core.util.Key;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface NetWorkUser {
    boolean isOnline();

    Channel nettyChannel();

    Plugin plugin();

    String name();

    void setName(String name);

    UUID uuid();

    void setUUID(UUID uuid);

    void sendPacket(Object packet, boolean immediately);

    void receivePacket(Object packet);

    @ApiStatus.Internal
    ConnectionState decoderState();

    @ApiStatus.Internal
    ConnectionState encoderState();

    int clientSideSectionCount();

    Key clientSideDimension();

    Object serverPlayer();

    Object platformPlayer();

    Map<Integer, List<Integer>> furnitureView();

    Map<Integer, Object> entityView();

    boolean clientModEnabled();

    void setClientModState(boolean enable);

    void setCurrentResourcePackUUID(UUID uuid);

    @Nullable
    UUID currentResourcePackUUID();
}
