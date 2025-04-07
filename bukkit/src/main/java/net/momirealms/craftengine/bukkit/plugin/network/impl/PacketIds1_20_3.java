package net.momirealms.craftengine.bukkit.plugin.network.impl;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;

public class PacketIds1_20_3 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return 9;
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return 71;
    }

    @Override
    public int clientboundLevelParticlesPacket() {
        return 39;
    }

    @Override
    public int clientboundLevelEventPacket() {
        return 38;
    }

    @Override
    public int clientboundAddEntityPacket() {
        return 1;
    }

    @Override
    public int clientboundOpenScreenPacket() {
        return 49;
    }

    @Override
    public int clientboundSoundPacket() {
        return 102;
    }

    @Override
    public int clientboundRemoveEntitiesPacket() {
        return 64;
    }

    @Override
    public int clientboundSetEntityDataPacket() {
        return 86;
    }
}
