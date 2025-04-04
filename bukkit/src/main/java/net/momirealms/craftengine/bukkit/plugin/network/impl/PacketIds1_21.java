package net.momirealms.craftengine.bukkit.plugin.network.impl;

import net.momirealms.craftengine.bukkit.plugin.network.PacketIds;

public class PacketIds1_21 implements PacketIds {

    @Override
    public int clientboundBlockUpdatePacket() {
        return PacketIdsFind.getClientboundPackets("minecraft:block_update");
    }

    @Override
    public int clientboundSectionBlocksUpdatePacket() {
        return PacketIdsFind.getClientboundPackets("minecraft:section_blocks_update");
    }

    @Override
    public int clientboundLevelParticlesPacket() {
        return PacketIdsFind.getClientboundPackets("minecraft:level_particles");
    }

    @Override
    public int clientboundLevelEventPacket() {
        return PacketIdsFind.getClientboundPackets("minecraft:level_event");
    }

    @Override
    public int clientboundAddEntityPacket() {
        return PacketIdsFind.getClientboundPackets("minecraft:add_entity");
    }
}
