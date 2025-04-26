package net.momirealms.craftengine.core.util;

import net.momirealms.craftengine.core.plugin.network.ProtocolVersion;

public class ProtocolVersionUtils {

    public static boolean isVersionNewerThan(ProtocolVersion version, ProtocolVersion targetVersion) {
        return version.getId() >= targetVersion.getId();
    }
}
