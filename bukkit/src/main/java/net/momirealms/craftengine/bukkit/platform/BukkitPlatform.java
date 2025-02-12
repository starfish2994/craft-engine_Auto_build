package net.momirealms.craftengine.bukkit.platform;

import net.momirealms.craftengine.bukkit.api.event.AsyncGenerateResourcePackEndEvent;
import net.momirealms.craftengine.bukkit.api.event.AsyncGenerateResourcePackStartEvent;
import net.momirealms.craftengine.bukkit.util.EventUtils;
import net.momirealms.craftengine.core.platform.Platform;

import java.nio.file.Path;

public class BukkitPlatform implements Platform {
    @Override
    public boolean AsyncGenerateResourcePackStartEvent(Path generatedPackPath, Path zipFile) {
        AsyncGenerateResourcePackStartEvent startEvent = new AsyncGenerateResourcePackStartEvent(generatedPackPath, zipFile);
        return EventUtils.fireAndCheckCancel(startEvent);
    }

    @Override
    public void AsyncGenerateResourcePackEndEvent(Path generatedPackPath, Path zipFile) {
        AsyncGenerateResourcePackEndEvent endEvent = new AsyncGenerateResourcePackEndEvent(generatedPackPath, zipFile);
        EventUtils.fireAndForget(endEvent);
    }
}