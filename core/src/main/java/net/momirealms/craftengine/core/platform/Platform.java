package net.momirealms.craftengine.core.platform;

import java.nio.file.Path;

public interface Platform {
    boolean AsyncGenerateResourcePackStartEvent(Path generatedPackPath, Path zipFile);
    void AsyncGenerateResourcePackEndEvent(Path generatedPackPath, Path zipFile);
}
