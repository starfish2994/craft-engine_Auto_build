package net.momirealms.craftengine.bukkit.plugin.network.id;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.util.HashMap;
import java.util.Map;

public class PacketIdFinder {
    private static final Map<String, Map<String, Integer>> gamePacketIdsByName = new HashMap<>();
    private static final Map<String, Map<Class<?>, Integer>> gamePacketIdsByClazz = new HashMap<>();

    static {
        try {
            if (VersionHelper.isOrAbove1_21()) {
                Object packetReport = Reflections.constructor$PacketReport.newInstance((Object) null);
                JsonElement jsonElement = (JsonElement) Reflections.method$PacketReport$serializePackets.invoke(packetReport);
                var play = jsonElement.getAsJsonObject().get("play");
                for (var entry : play.getAsJsonObject().entrySet()) {
                    Map<String, Integer> ids = new HashMap<>();
                    gamePacketIdsByName.put(entry.getKey(), ids);
                    for (var entry2 : entry.getValue().getAsJsonObject().entrySet()) {
                        ids.put(entry2.getKey(), entry2.getValue().getAsJsonObject().get("protocol_id").getAsInt());
                    }
                }
            } else if (VersionHelper.isOrAbove1_20_5()) {
                gamePacketIdsByName.putAll(FastNMS.INSTANCE.method$getGamePacketIdsByName());
            } else {
                gamePacketIdsByClazz.putAll(FastNMS.INSTANCE.method$getGamePacketIdsByClazz());
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to get packets", e);
        }
    }

    public static int clientboundByName(String packetName) {
        return gamePacketIdsByName.get("clientbound").getOrDefault(packetName, -1);
    }

    public static int clientboundByClazz(Class<?> clazz) {
        return gamePacketIdsByClazz.get("clientbound").getOrDefault(clazz, -1);
    }
}
