package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.CoreReflections;
import net.momirealms.craftengine.bukkit.plugin.reflection.minecraft.NetworkReflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.UUID;

public final class ResourcePackUtils {
    private ResourcePackUtils() {}

    public static Object createPacket(UUID uuid, String url, String hash) {
        return FastNMS.INSTANCE.constructor$ClientboundResourcePackPushPacket(uuid, url, hash, Config.kickOnDeclined(), ComponentUtils.adventureToMinecraft(Config.resourcePackPrompt()));
    }

    public static Object createServerResourcePackInfo(UUID uuid, String url, String hash) {
        return FastNMS.INSTANCE.constructor$ServerResourcePackInfo(uuid, url, hash, Config.kickOnDeclined(), ComponentUtils.adventureToMinecraft(Config.resourcePackPrompt()));
    }

    public static void finishCurrentTask(Object packetListener, Object type) {
        try {
            CoreReflections.methodHandle$ServerConfigurationPacketListenerImpl$finishCurrentTask.invokeExact(packetListener, type);
        } catch (Throwable e) {
            CraftEngine.instance().logger().warn("Failed to finish current task", e);
        }
    }

    public static void handleResourcePackResponse(Object packetListener, Object packet, Object action) {
        try {
            NetworkReflections.methodHandle$ServerCommonPacketListener$handleResourcePackResponse.invokeExact(packetListener, packet);
            if (action != NetworkReflections.instance$ServerboundResourcePackPacket$Action$ACCEPTED
                    && action != NetworkReflections.instance$ServerboundResourcePackPacket$Action$DOWNLOADED) {
                ResourcePackUtils.finishCurrentTask(packetListener, CoreReflections.instance$ServerResourcePackConfigurationTask$TYPE);
            }
        } catch (Throwable e) {
            CraftEngine.instance().logger().warn("Failed to handle ServerboundResourcePackPacket", e);
        }
    }
}
