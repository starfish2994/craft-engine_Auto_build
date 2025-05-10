package net.momirealms.craftengine.core.plugin.network;

import it.unimi.dsi.fastutil.ints.IntList;

public interface EntityPacketHandler {

    default boolean handleEntitiesRemove(IntList entityIds) {
        return false;
    }

    default void handleSetEntityData(NetWorkUser user, ByteBufPacketEvent event) {
    }
}
