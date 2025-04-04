package net.momirealms.craftengine.bukkit.plugin.network.impl;

import com.google.gson.JsonElement;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.util.HashMap;
import java.util.Map;

public class PacketIdFinder {
    private static final Map<String, Map<String, Integer>> gamePacketIds = new HashMap<>();

    static {
        try {
            Object packetReport = Reflections.constructor$PacketReport.newInstance((Object) null);
            JsonElement jsonElement = (JsonElement) Reflections.method$PacketReport$serializePackets.invoke(packetReport);
            var play = jsonElement.getAsJsonObject().get("play");
            for (var entry : play.getAsJsonObject().entrySet()) {
                Map<String, Integer> ids = new HashMap<>();
                gamePacketIds.put(entry.getKey(), ids);
                for (var entry2 : entry.getValue().getAsJsonObject().entrySet()) {
                    ids.put(entry2.getKey(), entry2.getValue().getAsJsonObject().get("protocol_id").getAsInt());
                }
            }
        } catch (Exception e) {
            CraftEngine.instance().logger().warn("Failed to get packets", e);
        }
    }

    public static int clientboundByName(String packetName) {
        return gamePacketIds.get("clientbound").get(packetName);
    }
}
