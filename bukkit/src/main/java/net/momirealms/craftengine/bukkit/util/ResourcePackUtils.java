package net.momirealms.craftengine.bukkit.util;

import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.VersionHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.UUID;

public class ResourcePackUtils {

    public static Object createPacket(UUID uuid, String url, String hash) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (VersionHelper.isVersionNewerThan1_20_5()) {
            return Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(uuid, url, hash, Config.kickOnDeclined(), Optional.of(ComponentUtils.adventureToMinecraft(Config.resourcePackPrompt())));
        } else if (VersionHelper.isVersionNewerThan1_20_3()) {
            return Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(uuid, url, hash, Config.kickOnDeclined(), ComponentUtils.adventureToMinecraft(Config.resourcePackPrompt()));
        } else {
            return Reflections.constructor$ClientboundResourcePackPushPacket.newInstance(url, hash, Config.kickOnDeclined(), ComponentUtils.adventureToMinecraft(Config.resourcePackPrompt()));
        }
    }
}
