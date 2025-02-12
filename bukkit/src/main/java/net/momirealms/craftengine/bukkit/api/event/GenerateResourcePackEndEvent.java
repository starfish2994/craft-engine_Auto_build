package net.momirealms.craftengine.bukkit.api.event;

import java.nio.file.Path;

public class GenerateResourcePackEndEvent extends GenerateResourcePackEvent {
    public GenerateResourcePackEndEvent(Path generatedPackPath, Path zipFile) {
        super(generatedPackPath, zipFile);
    }
}
