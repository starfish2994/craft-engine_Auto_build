package net.momirealms.craftengine.core.pack.host;

import net.momirealms.craftengine.core.plugin.CraftEngine;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ResourcePackHost {

    CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player);

    CompletableFuture<Void> upload(Path resourcePackPath);

    static Path customPackPath(String path) {
        return path.startsWith(".") ? CraftEngine.instance().dataFolderPath().resolve(path) : Path.of(path);
    }
}
