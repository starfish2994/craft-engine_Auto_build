package net.momirealms.craftengine.bukkit.api.event;

import org.bukkit.event.Cancellable;

import java.nio.file.Path;

public class GenerateResourcePackStartEvent extends GenerateResourcePackEvent implements Cancellable {
    private boolean cancelled;

    public GenerateResourcePackStartEvent(Path generatedPackPath, Path zipFile) {
        super(generatedPackPath, zipFile);
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
