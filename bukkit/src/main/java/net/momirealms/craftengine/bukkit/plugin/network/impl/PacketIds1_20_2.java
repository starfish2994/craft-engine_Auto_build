package net.momirealms.craftengine.bukkit.plugin.network.impl;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;

public class PacketIds1_20_2 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return 9;
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return 69;
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
        return 100;
    }

    @Override
    public int clientboundRemoveEntitiesPacket() {
        return 64;
    }

    @Override
    public int clientboundSetEntityDataPacket() {
        return 84;
    }

    @Override
    public int clientboundSetTitleTextPacket() {
        return 97;
    }

    @Override
    public int clientboundSetSubtitleTextPacket() {
        return 95;
    }

    @Override
    public int clientboundSetActionBarTextPacket() {
        return 72;
    }

    @Override
    public int clientboundBossEventPacket() {
        return 10;
    }
}
