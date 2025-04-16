package net.momirealms.craftengine.core.pack.host;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ResourcePackHost {

    CompletableFuture<ResourcePackDownloadData> requestResourcePackDownloadLink(UUID player);

    ResourcePackDownloadData getResourcePackDownloadLink(UUID player);
}
