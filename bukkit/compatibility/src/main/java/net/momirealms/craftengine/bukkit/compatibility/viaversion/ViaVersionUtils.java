package net.momirealms.craftengine.bukkit.compatibility.viaversion;

import com.viaversion.viaversion.api.Via;

import java.util.UUID;

public final class ViaVersionUtils {

    private ViaVersionUtils() {}

    public static int getPlayerProtocolVersion(UUID uuid) {
        return Via.getAPI().getPlayerProtocolVersion(uuid).getVersion();
    }
}
