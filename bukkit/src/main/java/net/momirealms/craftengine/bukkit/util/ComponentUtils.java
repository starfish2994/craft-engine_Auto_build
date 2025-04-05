package net.momirealms.craftengine.bukkit.util;

import com.google.gson.JsonElement;
import net.kyori.adventure.text.Component;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.util.AdventureHelper;

public class ComponentUtils {

    private ComponentUtils() {}

    public static Object adventureToMinecraft(Component component) {
        return jsonElementToMinecraft(AdventureHelper.componentToJsonElement(component));
    }

    public static Object jsonElementToMinecraft(JsonElement json) {
        return FastNMS.INSTANCE.method$Component$Serializer$fromJson(json);
    }

    public static Object jsonToMinecraft(String json) {
        return FastNMS.INSTANCE.method$Component$Serializer$fromJson(json);
    }

    public static String minecraftToJson(Object component) {
        return FastNMS.INSTANCE.method$Component$Serializer$toJson(component);
    }

    public static String paperAdventureToJson(Object component) {
        try {
            return (String) Reflections.method$ComponentSerializer$serialize.invoke(Reflections.instance$GsonComponentSerializer, component);
        } catch (ReflectiveOperationException e) {
            CraftEngine.instance().logger().warn("Failed to serialize paper adventure component " + component, e);
            return AdventureHelper.EMPTY_COMPONENT;
        }
    }

    public static Object jsonToPaperAdventure(String json) {
        try {
            return Reflections.method$ComponentSerializer$deserialize.invoke(Reflections.instance$GsonComponentSerializer, json);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to deserialize paper component from json", e);
        }
    }
}
