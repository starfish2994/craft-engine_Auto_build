package net.momirealms.craftengine.mod.util;

import net.minecraft.server.MinecraftServer;

public class VersionHelper {
    public static final boolean v1_21_2;

    static {
        String version = MinecraftServer.getServer().getServerVersion();
        String[] split = version.split("\\.");
        float versionF = Float.parseFloat(split[1] + "." + (split.length == 3 ? split[2] : "0"));
        v1_21_2 = versionF >= 21.2f;
    }

    public static boolean above1_21_2() {
        return v1_21_2;
    }
}
