package net.momirealms.craftengine.core.pack.host.impl;

import net.momirealms.craftengine.core.pack.host.ResourcePackDownloadData;
import net.momirealms.craftengine.core.pack.host.ResourcePackHost;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SelfHost implements ResourcePackHost {

    @Override
    public CompletableFuture<ResourcePackDownloadData> requestResourcePackDownloadLink(UUID player) {
        return null;
    }

    @Override
    public ResourcePackDownloadData getResourcePackDownloadLink(UUID player) {
        return null;
    }

    @Override
    public CompletableFuture<Boolean> upload(Path resourcePackPath) {
        return null;
    }
}
