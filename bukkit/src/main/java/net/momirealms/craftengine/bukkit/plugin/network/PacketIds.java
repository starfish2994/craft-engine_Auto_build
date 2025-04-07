package net.momirealms.craftengine.bukkit.plugin.network;

public interface PacketIds {

    int clientboundBlockUpdatePacket();

    int clientboundSectionBlocksUpdatePacket();

    int clientboundLevelParticlesPacket();

    int clientboundLevelEventPacket();

    int clientboundAddEntityPacket();

    int clientboundOpenScreenPacket();

    int clientboundEntityPositionSyncPacket();

    int clientboundRemoveEntitiesPacket();
}
