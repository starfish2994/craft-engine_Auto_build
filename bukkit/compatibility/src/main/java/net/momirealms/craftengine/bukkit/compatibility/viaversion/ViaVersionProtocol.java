package net.momirealms.craftengine.bukkit.compatibility.viaversion;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.ViaAPI;

import java.util.UUID;


public class ViaVersionProtocol {
    private final boolean hasPlugin;
    private final ViaAPI<?> viaAPI;

    public ViaVersionProtocol(boolean hasPlugin) {
        this.hasPlugin = hasPlugin;
        this.viaAPI = hasPlugin ? Via.getAPI() : null;
    }

    public int getPlayerProtocolVersion(UUID uuid) {
        if (!hasPlugin) return -1;
        System.out.println(this.viaAPI.getPlayerProtocolVersion(uuid).getVersion());
        return this.viaAPI.getPlayerProtocolVersion(uuid).getVersion();
    }

    public boolean hasPlugin() {
        return hasPlugin;
    }
}
