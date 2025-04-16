package net.momirealms.craftengine.core.pack.host;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ResourcePackHost {

    CompletableFuture<ResourcePackDownloadData> requestOneTimeUrl(UUID player);

    ResourcePackDownloadData getResourcePackUrl(UUID player);
}
