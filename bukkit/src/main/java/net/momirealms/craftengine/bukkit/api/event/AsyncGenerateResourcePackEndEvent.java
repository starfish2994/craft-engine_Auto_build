package net.momirealms.craftengine.bukkit.api.event;

import java.nio.file.Path;

public class AsyncGenerateResourcePackEndEvent extends AsyncGenerateResourcePackEvent {
    public AsyncGenerateResourcePackEndEvent(Path generatedPackPath, Path zipFile) {
        super(generatedPackPath, zipFile);
    }
}
