package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SimpleExternalHost implements ResourcePackHost {
    private final ResourcePackDownloadData downloadData;

    public SimpleExternalHost(ResourcePackDownloadData downloadData) {
        this.downloadData = downloadData;
    }

    @Override
    public CompletableFuture<ResourcePackDownloadData> requestResourcePackDownloadLink(UUID player) {
        return CompletableFuture.completedFuture(this.downloadData);
    }

    @Override
    public ResourcePackDownloadData getResourcePackDownloadLink(UUID player) {
        return this.downloadData;
    }

    @Override
    public CompletableFuture<Boolean> upload(Path resourcePackPath) {
        return CompletableFuture.completedFuture(true);
    }
}
