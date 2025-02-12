package net.momirealms.craftengine.bukkit.platform;

import net.momirealms.craftengine.bukkit.api.event.AsyncGenerateResourcePackEvent;
import net.momirealms.craftengine.bukkit.util.EventUtils;
import net.momirealms.craftengine.core.platform.Platform;

import java.nio.file.Path;

public class BukkitPlatform implements Platform {

    @Override
    public void asyncGenerateResourcePackEvent(Path generatedPackPath, Path zipFile) {
        AsyncGenerateResourcePackEvent endEvent = new AsyncGenerateResourcePackEvent(generatedPackPath, zipFile);
        EventUtils.fireAndForget(endEvent);
    }
}