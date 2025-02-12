package net.momirealms.craftengine.bukkit.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class AsyncGenerateResourcePackEvent extends Event {
    private static final HandlerList handlerList = new HandlerList();
    private final Path generatedPackPath;
    private final Path zipFile;

    public AsyncGenerateResourcePackEvent(@NotNull Path generatedPackPath, @NotNull Path zipFile) {
        super(true);
        this.generatedPackPath = generatedPackPath;
        this.zipFile = zipFile;
    }

    public @NotNull Path generatedPackPath() {
        return generatedPackPath;
    }

    public @NotNull Path zipFile() {
        return zipFile;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    public @NotNull HandlerList getHandlers() {
        return getHandlerList();
    }
}
