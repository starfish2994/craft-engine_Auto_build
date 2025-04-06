package net.momirealms.craftengine.bukkit.plugin.network.impl;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;

public class PacketIds1_21 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return PacketIdFinder.clientboundByName("minecraft:block_update");
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return PacketIdFinder.clientboundByName("minecraft:section_blocks_update");
    }

    @Override
    public int clientboundLevelParticlesPacket() {
        return PacketIdFinder.clientboundByName("minecraft:level_particles");
    }

    @Override
    public int clientboundLevelEventPacket() {
        return PacketIdFinder.clientboundByName("minecraft:level_event");
    }

    @Override
    public int clientboundAddEntityPacket() {
        return PacketIdFinder.clientboundByName("minecraft:add_entity");
    }

    @Override
    public int clientboundOpenScreenPacket() {
        return PacketIdFinder.clientboundByName("minecraft:open_screen");
    }
}
