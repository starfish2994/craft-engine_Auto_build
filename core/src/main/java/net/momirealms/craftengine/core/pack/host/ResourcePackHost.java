package net.momirealms.craftengine.core.pack.host;

import net.momirealms.craftengine.core.util.Key;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ResourcePackHost {

    CompletableFuture<List<ResourcePackDownloadData>> requestResourcePackDownloadLink(UUID player);

    CompletableFuture<Void> upload(Path resourcePackPath);

    boolean canUpload();

    Key type();
}
