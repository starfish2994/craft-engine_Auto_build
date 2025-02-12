package net.momirealms.craftengine.core.platform;

import java.nio.file.Path;

public interface Platform {

    void asyncGenerateResourcePackEvent(Path generatedPackPath, Path zipFile);
}
